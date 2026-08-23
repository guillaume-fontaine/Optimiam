package fr.trollgun.optimiam.shopping.application;

import fr.trollgun.optimiam.common.event.DomainEvent;
import fr.trollgun.optimiam.common.event.DomainEventPublisher;
import fr.trollgun.optimiam.planning.domain.MealPlan;
import fr.trollgun.optimiam.planning.domain.MealPlanRepository;
import fr.trollgun.optimiam.planning.domain.MealPlanStatus;
import fr.trollgun.optimiam.planning.domain.MealType;
import fr.trollgun.optimiam.product.application.ProductService;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.recipe.domain.RecipeIngredient;
import fr.trollgun.optimiam.shopping.api.dto.GenerateShoppingListRequest;
import fr.trollgun.optimiam.shopping.api.dto.ShoppingListResponse;
import fr.trollgun.optimiam.shopping.domain.ShoppingList;
import fr.trollgun.optimiam.shopping.domain.ShoppingListItem;
import fr.trollgun.optimiam.shopping.domain.ShoppingListItemRepository;
import fr.trollgun.optimiam.shopping.domain.ShoppingListRepository;
import fr.trollgun.optimiam.shopping.domain.ShoppingListStatus;
import fr.trollgun.optimiam.stock.application.StockService;
import fr.trollgun.optimiam.stock.domain.StockItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingListServiceTest {

    @Mock
    private ShoppingListRepository shoppingListRepository;

    @Mock
    private ShoppingListItemRepository shoppingListItemRepository;

    @Mock
    private MealPlanRepository mealPlanRepository;

    @Mock
    private StockService stockService;

    @Mock
    private ProductService productService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private ShoppingListService shoppingListService;

    private Product tomate;
    private Recipe recipe;
    private MealPlan mealPlan;

    @BeforeEach
    void setUp() {
        tomate = Product.builder()
                .id(UUID.randomUUID())
                .name("Tomate")
                .defaultUnit(Unit.KG)
                .build();

        recipe = Recipe.builder()
                .id(UUID.randomUUID())
                .name("Salade")
                .servings(2)
                .build();
        recipe.addIngredient(RecipeIngredient.builder().product(tomate).quantity(new BigDecimal("0.500")).unit(Unit.KG).build());

        mealPlan = MealPlan.builder()
                .id(UUID.randomUUID())
                .date(LocalDate.now())
                .mealType(MealType.LUNCH)
                .recipe(recipe)
                .servings(2)
                .status(MealPlanStatus.PLANNED)
                .build();
    }

    @Test
    @DisplayName("Doit calculer la quantité manquante nette (Requis 500g - Stock 200g = Manque 300g)")
    void shouldGenerateShoppingListWithNetMissingQuantity() {
        StockItem stockTomates = StockItem.builder()
                .id(UUID.randomUUID())
                .product(tomate)
                .quantity(new BigDecimal("0.200"))
                .unit(Unit.KG)
                .build();

        when(mealPlanRepository.findByDateBetweenWithDetails(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(mealPlan));
        when(stockService.getAllAvailableStock()).thenReturn(List.of(stockTomates));
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(i -> {
            ShoppingList s = i.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        ShoppingListResponse response = shoppingListService.generateFromPlanning(GenerateShoppingListRequest.builder().build());

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(1);
        assertThat(response.getItems().get(0).getMissingQuantity()).isEqualByComparingTo(new BigDecimal("0.300"));
        assertThat(response.getItems().get(0).getRequiredQuantity()).isEqualByComparingTo(new BigDecimal("0.500"));
        assertThat(response.getItems().get(0).getStockQuantity()).isEqualByComparingTo(new BigDecimal("0.200"));
        verify(domainEventPublisher).publish(any(DomainEvent.class));
    }

    @Test
    @DisplayName("Doit valider les achats cochés et les ajouter au stock")
    void shouldValidatePurchases() {
        UUID listId = UUID.randomUUID();
        ShoppingListItem item = ShoppingListItem.builder()
                .id(UUID.randomUUID())
                .product(tomate)
                .missingQuantity(new BigDecimal("0.500"))
                .unit(Unit.KG)
                .checked(true)
                .build();

        ShoppingList list = ShoppingList.builder()
                .id(listId)
                .name("Courses")
                .items(List.of(item))
                .status(ShoppingListStatus.ACTIVE)
                .build();

        when(shoppingListRepository.findById(listId)).thenReturn(Optional.of(list));
        when(shoppingListRepository.save(any(ShoppingList.class))).thenAnswer(i -> i.getArgument(0));

        ShoppingListResponse response = shoppingListService.validatePurchases(listId);

        assertThat(response.getStatus()).isEqualTo(ShoppingListStatus.COMPLETED);
        verify(stockService).createStockEntry(any());
        verify(domainEventPublisher).publish(any(DomainEvent.class));
    }
}
