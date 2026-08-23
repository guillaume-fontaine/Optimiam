package fr.trollgun.optimiam.shopping.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.trollgun.optimiam.planning.domain.MealPlanRepository;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.domain.RecipeRepository;
import fr.trollgun.optimiam.shopping.api.dto.AddShoppingItemRequest;
import fr.trollgun.optimiam.shopping.api.dto.GenerateShoppingListRequest;
import fr.trollgun.optimiam.shopping.domain.ShoppingList;
import fr.trollgun.optimiam.shopping.domain.ShoppingListItem;
import fr.trollgun.optimiam.shopping.domain.ShoppingListItemRepository;
import fr.trollgun.optimiam.shopping.domain.ShoppingListRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class ShoppingListControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

    @Autowired
    private ShoppingListItemRepository shoppingListItemRepository;

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
    private Product tomate;

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
        tomate = productRepository.save(Product.builder().name("Tomate").defaultUnit(Unit.KG).category(category).build());
    }

    @Test
    @DisplayName("POST /api/v1/shopping-lists/generate - Doit générer une liste de courses")
    void shouldGenerateShoppingList() throws Exception {
        GenerateShoppingListRequest request = GenerateShoppingListRequest.builder()
                .name("Courses Test")
                .build();

        mockMvc.perform(post("/api/v1/shopping-lists/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Courses Test")));
    }

    @Test
    @DisplayName("POST /api/v1/shopping-lists/{id}/items - Doit ajouter un article libre")
    void shouldAddItemToList() throws Exception {
        ShoppingList list = shoppingListRepository.save(ShoppingList.builder().name("Ma Liste").build());

        AddShoppingItemRequest itemReq = AddShoppingItemRequest.builder()
                .productId(tomate.getId())
                .quantity(new BigDecimal("1.500"))
                .unit(Unit.KG)
                .build();

        mockMvc.perform(post("/api/v1/shopping-lists/" + list.getId() + "/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(itemReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].product.name", is("Tomate")))
                .andExpect(jsonPath("$.items[0].missingQuantity", is(1.5)));
    }
}
