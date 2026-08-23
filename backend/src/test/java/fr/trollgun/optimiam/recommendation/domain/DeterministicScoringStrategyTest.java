package fr.trollgun.optimiam.recommendation.domain;

import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.domain.Difficulty;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.recipe.domain.RecipeIngredient;
import fr.trollgun.optimiam.recommendation.api.dto.RecommendationResponse;
import fr.trollgun.optimiam.stock.domain.Location;
import fr.trollgun.optimiam.stock.domain.StockItem;
import fr.trollgun.optimiam.stock.domain.StockStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class DeterministicScoringStrategyTest {

    private DeterministicScoringStrategy strategy;

    private Product tomate;
    private Product courgette;
    private Product oeufs;
    private Product fromage;

    private Recipe ratatouille;
    private Recipe omelette;

    private LocalDate today;

    @BeforeEach
    void setUp() {
        strategy = new DeterministicScoringStrategy();
        today = LocalDate.of(2026, 8, 23);

        tomate = Product.builder().id(UUID.randomUUID()).name("Tomate").defaultUnit(Unit.KG).build();
        courgette = Product.builder().id(UUID.randomUUID()).name("Courgette").defaultUnit(Unit.KG).build();
        oeufs = Product.builder().id(UUID.randomUUID()).name("Œufs").defaultUnit(Unit.PIECE).build();
        fromage = Product.builder().id(UUID.randomUUID()).name("Fromage").defaultUnit(Unit.G).build();

        // Recette 1 : Ratatouille (Tomates + Courgettes)
        ratatouille = Recipe.builder()
                .id(UUID.randomUUID())
                .name("Ratatouille provençale")
                .preparationTimeMinutes(20)
                .cookingTimeMinutes(30)
                .difficulty(Difficulty.EASY)
                .servings(4)
                .tags(Set.of("Anti-gaspi", "Végétarien"))
                .build();
        ratatouille.addIngredient(RecipeIngredient.builder().product(tomate).quantity(new BigDecimal("0.800")).unit(Unit.KG).build());
        ratatouille.addIngredient(RecipeIngredient.builder().product(courgette).quantity(new BigDecimal("0.500")).unit(Unit.KG).build());

        // Recette 2 : Omelette (Œufs + Fromage)
        omelette = Recipe.builder()
                .id(UUID.randomUUID())
                .name("Omelette au fromage")
                .preparationTimeMinutes(10)
                .cookingTimeMinutes(5)
                .difficulty(Difficulty.EASY)
                .servings(2)
                .tags(Set.of("Rapide"))
                .build();
        omelette.addIngredient(RecipeIngredient.builder().product(oeufs).quantity(new BigDecimal("4")).unit(Unit.PIECE).build());
        omelette.addIngredient(RecipeIngredient.builder().product(fromage).quantity(new BigDecimal("50")).unit(Unit.G).build());
    }

    @Test
    @DisplayName("La Ratatouille doit être classée #1 grâce aux tomates et courgettes proches de la péremption")
    void shouldPrioritizeRecipeWithExpiringIngredients() {
        // Stock : Tomates expirent demain (bonus +40), Courgettes dans 2j (bonus +25)
        StockItem stockTomates = StockItem.builder()
                .id(UUID.randomUUID())
                .product(tomate)
                .quantity(new BigDecimal("1.000"))
                .unit(Unit.KG)
                .expirationDate(today.plusDays(1))
                .location(Location.FRIDGE)
                .status(StockStatus.AVAILABLE)
                .build();

        StockItem stockCourgettes = StockItem.builder()
                .id(UUID.randomUUID())
                .product(courgette)
                .quantity(new BigDecimal("0.600"))
                .unit(Unit.KG)
                .expirationDate(today.plusDays(2))
                .location(Location.FRIDGE)
                .status(StockStatus.AVAILABLE)
                .build();

        StockItem stockOeufs = StockItem.builder()
                .id(UUID.randomUUID())
                .product(oeufs)
                .quantity(new BigDecimal("6"))
                .unit(Unit.PIECE)
                .expirationDate(today.plusDays(15))
                .location(Location.FRIDGE)
                .status(StockStatus.AVAILABLE)
                .build();

        StockItem stockFromage = StockItem.builder()
                .id(UUID.randomUUID())
                .product(fromage)
                .quantity(new BigDecimal("100"))
                .unit(Unit.G)
                .expirationDate(today.plusDays(20))
                .location(Location.FRIDGE)
                .status(StockStatus.AVAILABLE)
                .build();

        RecommendationContext context = RecommendationContext.builder()
                .availableStock(List.of(stockTomates, stockCourgettes, stockOeufs, stockFromage))
                .recipes(List.of(omelette, ratatouille))
                .today(today)
                .build();

        List<RecommendationResponse> recommendations = strategy.computeRecommendations(context);

        assertThat(recommendations).hasSize(2);
        // Ratatouille en premier
        assertThat(recommendations.get(0).getRecipe().getName()).isEqualTo("Ratatouille provençale");
        assertThat(recommendations.get(0).getScore()).isGreaterThan(recommendations.get(1).getScore());
        assertThat(recommendations.get(0).getExpiringIngredientsUsed()).hasSize(2);
        assertThat(recommendations.get(0).getReasons()).anyMatch(r -> r.contains("Sauve 2 produit(s)"));
    }
}
