package fr.trollgun.optimiam.sync.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.trollgun.optimiam.planning.domain.MealPlanRepository;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.domain.RecipeRepository;
import fr.trollgun.optimiam.shopping.domain.ShoppingListItemRepository;
import fr.trollgun.optimiam.shopping.domain.ShoppingListRepository;
import fr.trollgun.optimiam.stock.domain.StockItemRepository;
import fr.trollgun.optimiam.sync.api.dto.SyncBatchRequest;
import fr.trollgun.optimiam.sync.api.dto.SyncOperationDto;
import fr.trollgun.optimiam.sync.domain.SyncOperationType;
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

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class SyncControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ShoppingListItemRepository shoppingListItemRepository;

    @Autowired
    private ShoppingListRepository shoppingListRepository;

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
    private Product product;

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
        product = productRepository.save(Product.builder().name("Tomate").defaultUnit(Unit.KG).category(category).build());
    }

    @Test
    @DisplayName("GET /api/v1/sync/status - Doit retourner le statut en ligne du serveur")
    void shouldReturnSyncStatus() throws Exception {
        mockMvc.perform(get("/api/v1/sync/status")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("ONLINE")))
                .andExpect(jsonPath("$.serverTime", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/v1/sync/batch - Doit créer une entrée de stock via le lot de sync")
    void shouldSyncStockEntryOperation() throws Exception {
        SyncOperationDto op = SyncOperationDto.builder()
                .operationId(UUID.randomUUID())
                .type(SyncOperationType.CREATE_STOCK_ENTRY)
                .payload(Map.of(
                        "productId", product.getId().toString(),
                        "quantity", 2.0,
                        "unit", "KG",
                        "location", "FRIDGE",
                        "entryDate", "2026-08-23",
                        "expirationDate", "2026-08-30"
                ))
                .build();

        SyncBatchRequest request = SyncBatchRequest.builder()
                .clientId("pwa-mobile")
                .operations(List.of(op))
                .build();

        mockMvc.perform(post("/api/v1/sync/batch")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncedCount", is(1)))
                .andExpect(jsonPath("$.conflictCount", is(0)))
                .andExpect(jsonPath("$.results[0].success", is(true)));
    }
}
