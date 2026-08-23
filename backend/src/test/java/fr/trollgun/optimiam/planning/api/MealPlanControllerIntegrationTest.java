package fr.trollgun.optimiam.planning.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.trollgun.optimiam.planning.api.dto.CreateMealPlanRequest;
import fr.trollgun.optimiam.planning.domain.MealPlan;
import fr.trollgun.optimiam.planning.domain.MealPlanRepository;
import fr.trollgun.optimiam.planning.domain.MealPlanStatus;
import fr.trollgun.optimiam.planning.domain.MealType;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.domain.Difficulty;
import fr.trollgun.optimiam.recipe.domain.Recipe;
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

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class MealPlanControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private MealPlanRepository mealPlanRepository;

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

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private MockMvc mockMvc;
    private Recipe recipe;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mealPlanRepository.deleteAll();
        recipeRepository.deleteAll();
        stockItemRepository.deleteAll();
        transactionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = categoryRepository.save(Category.builder().name("Fruits & Légumes").build());
        Product tomate = productRepository.save(Product.builder().name("Tomate").defaultUnit(Unit.KG).category(category).build());

        recipe = recipeRepository.save(Recipe.builder()
                .name("Salade de Tomates")
                .preparationTimeMinutes(5)
                .cookingTimeMinutes(0)
                .difficulty(Difficulty.EASY)
                .servings(2)
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/planning - Doit planifier un repas")
    void shouldCreateMealPlan() throws Exception {
        CreateMealPlanRequest request = CreateMealPlanRequest.builder()
                .date(LocalDate.now())
                .mealType(MealType.LUNCH)
                .recipeId(recipe.getId())
                .servings(2)
                .notes("Midi solo")
                .build();

        mockMvc.perform(post("/api/v1/planning")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.mealType", is("LUNCH")))
                .andExpect(jsonPath("$.recipe.name", is("Salade de Tomates")))
                .andExpect(jsonPath("$.status", is("PLANNED")));
    }

    @Test
    @DisplayName("GET /api/v1/planning - Doit lister les repas pour une plage de dates")
    void shouldGetMealPlans() throws Exception {
        LocalDate today = LocalDate.now();
        mealPlanRepository.save(MealPlan.builder()
                .date(today)
                .mealType(MealType.DINNER)
                .recipe(recipe)
                .servings(2)
                .status(MealPlanStatus.PLANNED)
                .build());

        mockMvc.perform(get("/api/v1/planning")
                        .param("startDate", today.minusDays(1).toString())
                        .param("endDate", today.plusDays(1).toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipe.name", is("Salade de Tomates")));
    }

    @Test
    @DisplayName("POST /api/v1/planning/{id}/cook - Doit marquer le repas comme cuisiné")
    void shouldMarkAsCooked() throws Exception {
        MealPlan mealPlan = mealPlanRepository.save(MealPlan.builder()
                .date(LocalDate.now())
                .mealType(MealType.DINNER)
                .recipe(recipe)
                .servings(2)
                .status(MealPlanStatus.PLANNED)
                .build());

        mockMvc.perform(post("/api/v1/planning/" + mealPlan.getId() + "/cook")
                        .param("deductStock", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COOKED")));
    }
}
