package fr.trollgun.optimiam.recommendation.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.domain.Difficulty;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.recipe.domain.RecipeIngredient;
import fr.trollgun.optimiam.recipe.domain.RecipeRepository;
import fr.trollgun.optimiam.recommendation.api.dto.RecommendationRequest;
import fr.trollgun.optimiam.stock.domain.Location;
import fr.trollgun.optimiam.stock.domain.StockItem;
import fr.trollgun.optimiam.stock.domain.StockItemRepository;
import fr.trollgun.optimiam.stock.domain.StockStatus;
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
import java.time.LocalDate;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class RecommendationControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private StockTransactionRepository transactionRepository;

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        shoppingListItemRepository.deleteAll();
        shoppingListRepository.deleteAll();
        mealPlanRepository.deleteAll();
        recipeRepository.deleteAll();
        stockItemRepository.deleteAll();
        transactionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = categoryRepository.save(Category.builder().name("Fruits & Légumes").build());
        Product tomate = productRepository.save(Product.builder().name("Tomate").defaultUnit(Unit.KG).category(category).build());

        stockItemRepository.save(StockItem.builder()
                .product(tomate)
                .quantity(new BigDecimal("1.000"))
                .unit(Unit.KG)
                .entryDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(1))
                .location(Location.FRIDGE)
                .status(StockStatus.AVAILABLE)
                .build());

        Recipe recipe = Recipe.builder()
                .name("Salade de Tomates")
                .preparationTimeMinutes(5)
                .cookingTimeMinutes(0)
                .difficulty(Difficulty.EASY)
                .servings(2)
                .tags(Set.of("Anti-gaspi"))
                .build();
        recipe.addIngredient(RecipeIngredient.builder().product(tomate).quantity(new BigDecimal("0.500")).unit(Unit.KG).build());
        recipeRepository.save(recipe);
    }

    @Test
    @DisplayName("POST /api/v1/recommendations - Doit retourner les recommandations scorées")
    void shouldReturnRecommendations() throws Exception {
        RecommendationRequest request = RecommendationRequest.builder()
                .onlyFullStock(true)
                .build();

        mockMvc.perform(post("/api/v1/recommendations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipe.name", is("Salade de Tomates")))
                .andExpect(jsonPath("$[0].fullyAvailableInStock", is(true)))
                .andExpect(jsonPath("$[0].reasons", not(empty())));
    }

    @Test
    @DisplayName("GET /api/v1/recommendations/daily - Doit retourner le top des suggestions du jour")
    void shouldReturnDailyRecommendations() throws Exception {
        mockMvc.perform(get("/api/v1/recommendations/daily")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }
}
