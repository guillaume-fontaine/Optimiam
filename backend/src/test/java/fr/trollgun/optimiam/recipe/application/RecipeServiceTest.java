package fr.trollgun.optimiam.recipe.application;

import fr.trollgun.optimiam.product.application.ProductService;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.api.dto.CreateRecipeIngredientRequest;
import fr.trollgun.optimiam.recipe.api.dto.CreateRecipeRequest;
import fr.trollgun.optimiam.recipe.api.dto.CreateRecipeStepRequest;
import fr.trollgun.optimiam.recipe.api.dto.RecipeResponse;
import fr.trollgun.optimiam.recipe.domain.Difficulty;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.recipe.domain.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private RecipeService recipeService;

    private Product product;
    private UUID productId;
    private UUID recipeId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        recipeId = UUID.randomUUID();

        product = Product.builder()
                .id(productId)
                .name("Courgette")
                .defaultUnit(Unit.KG)
                .build();
    }

    @Test
    @DisplayName("Doit créer une recette avec ses ingrédients et ses étapes")
    void shouldCreateRecipe() {
        CreateRecipeRequest request = CreateRecipeRequest.builder()
                .name("Poêlée de courgettes")
                .description("Délicieuse poêlée")
                .preparationTimeMinutes(10)
                .cookingTimeMinutes(15)
                .difficulty(Difficulty.EASY)
                .servings(2)
                .ingredients(List.of(
                        CreateRecipeIngredientRequest.builder()
                                .productId(productId)
                                .quantity(new BigDecimal("0.500"))
                                .unit(Unit.KG)
                                .build()
                ))
                .steps(List.of(
                        CreateRecipeStepRequest.builder()
                                .stepNumber(1)
                                .instruction("Couper les courgettes en rondelles")
                                .build()
                ))
                .build();

        when(productService.getProductEntity(productId)).thenReturn(product);
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe r = invocation.getArgument(0);
            r.setId(recipeId);
            return r;
        });

        RecipeResponse response = recipeService.createRecipe(request);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Poêlée de courgettes");
        assertThat(response.getIngredients()).hasSize(1);
        assertThat(response.getSteps()).hasSize(1);
        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    @DisplayName("Doit récupérer une recette par son ID")
    void shouldGetRecipeById() {
        Recipe recipe = Recipe.builder()
                .id(recipeId)
                .name("Ratatouille")
                .preparationTimeMinutes(20)
                .cookingTimeMinutes(30)
                .build();

        when(recipeRepository.findByIdWithIngredients(recipeId)).thenReturn(Optional.of(recipe));

        RecipeResponse response = recipeService.getRecipeById(recipeId);

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Ratatouille");
        assertThat(response.getTotalTimeMinutes()).isEqualTo(50);
    }
}
