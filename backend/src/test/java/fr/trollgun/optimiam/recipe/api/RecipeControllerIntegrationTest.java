package fr.trollgun.optimiam.recipe.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.api.dto.CreateRecipeIngredientRequest;
import fr.trollgun.optimiam.recipe.api.dto.CreateRecipeRequest;
import fr.trollgun.optimiam.recipe.api.dto.CreateRecipeStepRequest;
import fr.trollgun.optimiam.recipe.domain.Difficulty;
import fr.trollgun.optimiam.recipe.domain.RecipeRepository;
import fr.trollgun.optimiam.stock.domain.StockItemRepository;
import fr.trollgun.optimiam.transaction.domain.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class RecipeControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private StockTransactionRepository transactionRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private fr.trollgun.optimiam.planning.domain.MealPlanRepository mealPlanRepository;

    @Autowired
    private fr.trollgun.optimiam.shopping.domain.ShoppingListRepository shoppingListRepository;

    @Autowired
    private fr.trollgun.optimiam.shopping.domain.ShoppingListItemRepository shoppingListItemRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private MockMvc mockMvc;
    private Product product;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        shoppingListItemRepository.deleteAll();
        shoppingListRepository.deleteAll();
        mealPlanRepository.deleteAll();
        recipeRepository.deleteAll();
        transactionRepository.deleteAll();
        stockItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = categoryRepository.save(Category.builder()
                .name("Fruits & Légumes")
                .build());

        product = productRepository.save(Product.builder()
                .name("Courgette")
                .defaultUnit(Unit.KG)
                .category(category)
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/recipes - Doit créer une recette")
    void shouldCreateRecipe() throws Exception {
        CreateRecipeRequest request = CreateRecipeRequest.builder()
                .name("Courgettes sautées")
                .description("Simple et efficace")
                .preparationTimeMinutes(10)
                .cookingTimeMinutes(10)
                .difficulty(Difficulty.EASY)
                .servings(2)
                .ingredients(List.of(
                        CreateRecipeIngredientRequest.builder()
                                .productId(product.getId())
                                .quantity(new BigDecimal("0.400"))
                                .unit(Unit.KG)
                                .build()
                ))
                .steps(List.of(
                        CreateRecipeStepRequest.builder()
                                .stepNumber(1)
                                .instruction("Émincer les courgettes")
                                .build()
                ))
                .build();

        mockMvc.perform(post("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Courgettes sautées")))
                .andExpect(jsonPath("$.ingredients", hasSize(1)))
                .andExpect(jsonPath("$.steps", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/v1/recipes - Doit lister les recettes")
    void shouldGetRecipes() throws Exception {
        mockMvc.perform(get("/api/v1/recipes")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }
}
