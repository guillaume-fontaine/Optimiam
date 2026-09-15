package fr.trollgun.optimiam.shopping.application;

import fr.trollgun.optimiam.common.event.DomainEventPublisher;
import fr.trollgun.optimiam.common.exception.ErrorCode;
import fr.trollgun.optimiam.common.exception.ResourceNotFoundException;
import fr.trollgun.optimiam.planning.domain.MealPlan;
import fr.trollgun.optimiam.planning.domain.MealPlanRepository;
import fr.trollgun.optimiam.planning.domain.MealPlanStatus;
import fr.trollgun.optimiam.product.application.ProductService;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.recipe.domain.RecipeIngredient;
import fr.trollgun.optimiam.shopping.api.dto.*;
import fr.trollgun.optimiam.shopping.domain.ShoppingList;
import fr.trollgun.optimiam.shopping.domain.ShoppingListItem;
import fr.trollgun.optimiam.shopping.domain.ShoppingListItemRepository;
import fr.trollgun.optimiam.shopping.domain.ShoppingListRepository;
import fr.trollgun.optimiam.shopping.domain.ShoppingListStatus;
import fr.trollgun.optimiam.shopping.domain.event.PurchasesValidatedEvent;
import fr.trollgun.optimiam.shopping.domain.event.ShoppingListGeneratedEvent;
import fr.trollgun.optimiam.stock.api.dto.CreateStockEntryRequest;
import fr.trollgun.optimiam.stock.application.StockService;
import fr.trollgun.optimiam.stock.domain.Location;
import fr.trollgun.optimiam.stock.domain.StockItem;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShoppingListService {

    private final ShoppingListRepository shoppingListRepository;
    private final ShoppingListItemRepository shoppingListItemRepository;
    private final MealPlanRepository mealPlanRepository;
    private final StockService stockService;
    private final ProductService productService;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional
    public ShoppingListResponse generateFromPlanning(GenerateShoppingListRequest request) {
        LocalDate startDate = (request != null && request.getStartDate() != null)
                ? request.getStartDate()
                : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endDate = (request != null && request.getEndDate() != null)
                ? request.getEndDate()
                : startDate.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        String name = (request != null && request.getName() != null && !request.getName().isBlank())
                ? request.getName()
                : "Courses de la semaine (" + startDate + " au " + endDate + ")";

        // 1. Récupérer les repas planifiés actifs sur la période
        List<MealPlan> mealPlans = mealPlanRepository.findByDateBetweenWithDetails(startDate, endDate).stream()
                .filter(m -> m.getStatus() == MealPlanStatus.PLANNED)
                .collect(Collectors.toList());

        // 2. Agréger les ingrédients requis par produit
        Map<UUID, ProductRequirement> requirements = new HashMap<>();

        for (MealPlan plan : mealPlans) {
            Recipe recipe = plan.getRecipe();
            double ratio = (recipe.getServings() != null && recipe.getServings() > 0)
                    ? (double) plan.getServings() / recipe.getServings()
                    : 1.0;

            for (RecipeIngredient ingredient : recipe.getIngredients()) {
                if (ingredient.isOptional()) continue;

                Product product = ingredient.getProduct();
                BigDecimal neededQty = ingredient.getQuantity().multiply(BigDecimal.valueOf(ratio));
                Unit unit = ingredient.getUnit();

                requirements.compute(product.getId(), (id, existing) -> {
                    if (existing == null) {
                        return new ProductRequirement(product, neededQty, unit);
                    } else {
                        BigDecimal converted = convertQuantity(neededQty, unit, existing.unit);
                        existing.quantity = existing.quantity.add(converted);
                        return existing;
                    }
                });
            }
        }

        // 3. Récupérer le stock disponible
        List<StockItem> stockItems = stockService.getAllAvailableStock();
        Map<UUID, List<StockItem>> stockByProduct = stockItems.stream()
                .filter(item -> item.getQuantity() != null && item.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.groupingBy(item -> item.getProduct().getId()));

        // 4. Construire la nouvelle liste de courses
        ShoppingList shoppingList = ShoppingList.builder()
                .name(name)
                .startDate(startDate)
                .endDate(endDate)
                .status(ShoppingListStatus.ACTIVE)
                .build();

        for (ProductRequirement req : requirements.values()) {
            Product product = req.product;
            BigDecimal requiredQty = req.quantity.setScale(2, RoundingMode.HALF_UP);
            Unit unit = req.unit;

            List<StockItem> currentStock = stockByProduct.getOrDefault(product.getId(), Collections.emptyList());
            BigDecimal totalStock = BigDecimal.ZERO;

            for (StockItem stock : currentStock) {
                BigDecimal normalized = convertQuantity(stock.getQuantity(), stock.getUnit(), unit);
                totalStock = totalStock.add(normalized);
            }
            totalStock = totalStock.setScale(2, RoundingMode.HALF_UP);

            BigDecimal missingQty = requiredQty.subtract(totalStock).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);

            if (missingQty.compareTo(BigDecimal.ZERO) > 0) {
                ShoppingListItem item = ShoppingListItem.builder()
                        .product(product)
                        .requiredQuantity(requiredQty)
                        .stockQuantity(totalStock)
                        .missingQuantity(missingQty)
                        .unit(unit)
                        .checked(false)
                        .autoGenerated(true)
                        .build();

                shoppingList.addItem(item);
            }
        }

        ShoppingList saved = shoppingListRepository.save(shoppingList);
        domainEventPublisher.publish(new ShoppingListGeneratedEvent(saved));
        log.info("Liste de courses générée: {} articles manquants calculés", saved.getItems().size());

        return ShoppingListResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<ShoppingListResponse> getAllShoppingLists(ShoppingListStatus status) {
        List<ShoppingList> lists = (status != null)
                ? shoppingListRepository.findByStatusOrderByCreatedAtDesc(status)
                : shoppingListRepository.findAll();

        return lists.stream().map(ShoppingListResponse::from).collect(Collectors.toList());
    }

    @Transactional
    public ShoppingListResponse getActiveShoppingList() {
        Optional<ShoppingList> active = shoppingListRepository.findFirstByStatusOrderByCreatedAtDesc(ShoppingListStatus.ACTIVE);
        if (active.isPresent()) {
            return ShoppingListResponse.from(active.get());
        }
        // Auto-génération si aucune liste active
        return generateFromPlanning(GenerateShoppingListRequest.builder().build());
    }

    @Transactional(readOnly = true)
    public ShoppingListResponse getShoppingListById(UUID id) {
        return ShoppingListResponse.from(getShoppingListEntity(id));
    }

    @Transactional(readOnly = true)
    public ShoppingList getShoppingListEntity(UUID id) {
        return shoppingListRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Liste de courses introuvable: " + id, ErrorCode.SHOPPING_LIST_NOT_FOUND));
    }

    @Transactional
    public ShoppingListResponse addItemToList(UUID listId, AddShoppingItemRequest request) {
        ShoppingList list = getShoppingListEntity(listId);
        Product product = productService.getProductEntity(request.getProductId());
        Unit unit = request.getUnit() != null ? request.getUnit() : product.getDefaultUnit();

        ShoppingListItem item = ShoppingListItem.builder()
                .product(product)
                .requiredQuantity(request.getQuantity())
                .stockQuantity(BigDecimal.ZERO)
                .missingQuantity(request.getQuantity())
                .unit(unit)
                .checked(false)
                .autoGenerated(false)
                .build();

        list.addItem(item);
        ShoppingList updated = shoppingListRepository.save(list);
        log.info("Article ajouté manuellement à la liste de courses: {} x {}", request.getQuantity(), product.getName());

        return ShoppingListResponse.from(updated);
    }

    @Transactional
    public ShoppingListResponse updateItem(UUID listId, UUID itemId, UpdateShoppingItemRequest request) {
        ShoppingList list = getShoppingListEntity(listId);
        ShoppingListItem item = list.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Article introuvable dans la liste: " + itemId, ErrorCode.PRODUCT_NOT_FOUND));

        if (request.getQuantity() != null) {
            item.setMissingQuantity(request.getQuantity());
        }
        if (request.getPurchasedQuantity() != null) {
            item.setPurchasedQuantity(request.getPurchasedQuantity());
        }
        if (request.getChecked() != null) {
            item.setChecked(request.getChecked());
        }

        ShoppingList updated = shoppingListRepository.save(list);
        return ShoppingListResponse.from(updated);
    }

    @Transactional
    public ShoppingListResponse validatePurchases(UUID listId) {
        ShoppingList list = getShoppingListEntity(listId);
        List<ShoppingListItem> checkedItems = list.getItems().stream()
                .filter(ShoppingListItem::isChecked)
                .collect(Collectors.toList());

        int addedCount = 0;
        List<ShoppingListItem> completedItems = new ArrayList<>();
        for (ShoppingListItem item : checkedItems) {
            Product product = item.getProduct();
            int shelfLife = (product.getAverageShelfLifeDays() != null && product.getAverageShelfLifeDays() > 0)
                    ? product.getAverageShelfLifeDays()
                    : 7;

            Location location = Location.FRIDGE;
            if (product.getCategory() != null && product.getCategory().getName().toLowerCase().contains("féculent")) {
                location = Location.PANTRY;
            }

            stockService.createStockEntry(CreateStockEntryRequest.builder()
                    .productId(product.getId())
                    .quantity(item.getPurchasedQuantity() != null
                        ? item.getPurchasedQuantity()
                        : item.getMissingQuantity())
                    .unit(item.getUnit())
                    .entryDate(LocalDate.now())
                    .expirationDate(LocalDate.now().plusDays(shelfLife))
                    .location(location)
                    .build());
            addedCount++;

            BigDecimal purchasedQuantity = item.getPurchasedQuantity() != null
                    ? item.getPurchasedQuantity()
                    : item.getMissingQuantity();
            if (purchasedQuantity.compareTo(item.getMissingQuantity()) >= 0) {
                completedItems.add(item);
            } else {
                item.setMissingQuantity(item.getMissingQuantity().subtract(purchasedQuantity)
                        .max(BigDecimal.ZERO)
                        .setScale(2, RoundingMode.HALF_UP));
                item.setPurchasedQuantity(null);
                item.setChecked(false);
            }
        }

        completedItems.forEach(list::removeItem);
        list.setStatus(list.getItems().isEmpty()
            ? ShoppingListStatus.COMPLETED
            : ShoppingListStatus.ACTIVE);
        ShoppingList saved = shoppingListRepository.save(list);
        domainEventPublisher.publish(new PurchasesValidatedEvent(saved, addedCount));
        log.info("Validation des achats effectuée : {} articles transférés en stock", addedCount);

        return ShoppingListResponse.from(saved);
    }

    @Transactional
    public void deleteShoppingList(UUID id) {
        ShoppingList list = getShoppingListEntity(id);
        shoppingListRepository.delete(list);
        log.info("Liste de courses supprimée: {}", id);
    }

    private BigDecimal convertQuantity(BigDecimal quantity, Unit fromUnit, Unit toUnit) {
        if (quantity == null || fromUnit == null || toUnit == null) return BigDecimal.ZERO;
        if (fromUnit == toUnit) return quantity;

        if (fromUnit == Unit.KG && toUnit == Unit.G) return quantity.multiply(BigDecimal.valueOf(1000));
        if (fromUnit == Unit.G && toUnit == Unit.KG) return quantity.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
        if (fromUnit == Unit.L && toUnit == Unit.ML) return quantity.multiply(BigDecimal.valueOf(1000));
        if (fromUnit == Unit.ML && toUnit == Unit.L) return quantity.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

        return quantity;
    }

    private static class ProductRequirement {
        Product product;
        BigDecimal quantity;
        Unit unit;

        ProductRequirement(Product product, BigDecimal quantity, Unit unit) {
            this.product = product;
            this.quantity = quantity;
            this.unit = unit;
        }
    }
}
