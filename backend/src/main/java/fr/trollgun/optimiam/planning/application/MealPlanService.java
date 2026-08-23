package fr.trollgun.optimiam.planning.application;

import fr.trollgun.optimiam.common.event.DomainEventPublisher;
import fr.trollgun.optimiam.common.exception.ErrorCode;
import fr.trollgun.optimiam.common.exception.ResourceNotFoundException;
import fr.trollgun.optimiam.planning.api.dto.CreateMealPlanRequest;
import fr.trollgun.optimiam.planning.api.dto.MealPlanResponse;
import fr.trollgun.optimiam.planning.api.dto.UpdateMealPlanRequest;
import fr.trollgun.optimiam.planning.domain.MealPlan;
import fr.trollgun.optimiam.planning.domain.MealPlanRepository;
import fr.trollgun.optimiam.planning.domain.MealPlanStatus;
import fr.trollgun.optimiam.planning.domain.event.MealCookedEvent;
import fr.trollgun.optimiam.planning.domain.event.MealPlanCreatedEvent;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.application.RecipeService;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.recipe.domain.RecipeIngredient;
import fr.trollgun.optimiam.stock.api.dto.StockExitRequest;
import fr.trollgun.optimiam.stock.application.StockService;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final RecipeService recipeService;
    private final StockService stockService;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(readOnly = true)
    public List<MealPlanResponse> getMealPlans(LocalDate startDate, LocalDate endDate) {
        LocalDate start = (startDate != null) ? startDate : LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate end = (endDate != null) ? endDate : start.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));

        return mealPlanRepository.findByDateBetweenWithDetails(start, end).stream()
                .map(MealPlanResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MealPlanResponse getMealPlanById(UUID id) {
        return MealPlanResponse.from(getMealPlanEntity(id));
    }

    @Transactional(readOnly = true)
    public MealPlan getMealPlanEntity(UUID id) {
        return mealPlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Repas planifié introuvable avec l'identifiant: " + id, ErrorCode.MEAL_PLAN_NOT_FOUND));
    }

    @Transactional
    public MealPlanResponse createMealPlan(CreateMealPlanRequest request) {
        Recipe recipe = recipeService.getRecipeEntity(request.getRecipeId());

        MealPlan mealPlan = MealPlan.builder()
                .date(request.getDate())
                .mealType(request.getMealType())
                .recipe(recipe)
                .servings(request.getServings() != null ? request.getServings() : (recipe.getServings() != null ? recipe.getServings() : 4))
                .status(MealPlanStatus.PLANNED)
                .notes(request.getNotes())
                .build();

        MealPlan saved = mealPlanRepository.save(mealPlan);
        domainEventPublisher.publish(new MealPlanCreatedEvent(saved));
        log.info("Repas planifié : {} pour le {} [{}]", recipe.getName(), saved.getDate(), saved.getMealType());

        return MealPlanResponse.from(saved);
    }

    @Transactional
    public MealPlanResponse updateMealPlan(UUID id, UpdateMealPlanRequest request) {
        MealPlan mealPlan = getMealPlanEntity(id);
        Recipe recipe = recipeService.getRecipeEntity(request.getRecipeId());

        mealPlan.setDate(request.getDate());
        mealPlan.setMealType(request.getMealType());
        mealPlan.setRecipe(recipe);
        mealPlan.setServings(request.getServings());
        if (request.getStatus() != null) {
            mealPlan.setStatus(request.getStatus());
        }
        mealPlan.setNotes(request.getNotes());

        MealPlan updated = mealPlanRepository.save(mealPlan);
        log.info("Repas planifié mis à jour: {} [id={}]", recipe.getName(), updated.getId());

        return MealPlanResponse.from(updated);
    }

    @Transactional
    public MealPlanResponse markAsCooked(UUID id, boolean deductStock) {
        MealPlan mealPlan = getMealPlanEntity(id);
        mealPlan.setStatus(MealPlanStatus.COOKED);

        if (deductStock) {
            deductIngredientsFromStock(mealPlan);
        }

        MealPlan saved = mealPlanRepository.save(mealPlan);
        domainEventPublisher.publish(new MealCookedEvent(saved, deductStock));
        log.info("Repas validé comme cuisiné : {} (déduction stock={})", saved.getRecipe().getName(), deductStock);

        return MealPlanResponse.from(saved);
    }

    private void deductIngredientsFromStock(MealPlan mealPlan) {
        Recipe recipe = mealPlan.getRecipe();
        double ratio = (recipe.getServings() != null && recipe.getServings() > 0)
                ? (double) mealPlan.getServings() / recipe.getServings()
                : 1.0;

        List<StockItem> availableStock = stockService.getAllAvailableStock();

        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            if (ingredient.isOptional()) continue;

            BigDecimal neededQty = ingredient.getQuantity().multiply(BigDecimal.valueOf(ratio));
            UUID productId = ingredient.getProduct().getId();

            List<StockItem> matchingItems = availableStock.stream()
                    .filter(item -> item.getProduct().getId().equals(productId) && item.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                    .collect(Collectors.toList());

            BigDecimal remainingToDeduct = neededQty;

            for (StockItem stockItem : matchingItems) {
                if (remainingToDeduct.compareTo(BigDecimal.ZERO) <= 0) break;

                BigDecimal convertedStockQty = convertQuantity(stockItem.getQuantity(), stockItem.getUnit(), ingredient.getUnit());
                BigDecimal qtyToTakeConverted = convertedStockQty.min(remainingToDeduct);
                BigDecimal qtyToTakeInStockUnit = convertQuantity(qtyToTakeConverted, ingredient.getUnit(), stockItem.getUnit());

                try {
                    stockService.exitStock(stockItem.getId(), StockExitRequest.builder()
                            .quantity(qtyToTakeInStockUnit)
                            .reason("Cuisiné via le planning : " + recipe.getName())
                            .build());
                    remainingToDeduct = remainingToDeduct.subtract(qtyToTakeConverted);
                } catch (Exception e) {
                    log.warn("Impossible de déduire le stock pour {}: {}", stockItem.getProduct().getName(), e.getMessage());
                }
            }
        }
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

    @Transactional
    public void deleteMealPlan(UUID id) {
        MealPlan mealPlan = getMealPlanEntity(id);
        mealPlanRepository.delete(mealPlan);
        log.info("Repas planifié supprimé: {}", id);
    }
}
