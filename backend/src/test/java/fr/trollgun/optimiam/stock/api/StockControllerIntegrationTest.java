package fr.trollgun.optimiam.stock.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.stock.api.dto.CreateStockEntryRequest;
import fr.trollgun.optimiam.stock.api.dto.StockExitRequest;
import fr.trollgun.optimiam.stock.api.dto.StockLossRequest;
import fr.trollgun.optimiam.stock.domain.Location;
import fr.trollgun.optimiam.stock.domain.StockItem;
import fr.trollgun.optimiam.stock.domain.StockItemRepository;
import fr.trollgun.optimiam.stock.domain.StockStatus;
import fr.trollgun.optimiam.transaction.domain.LossReason;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class StockControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private StockItemRepository stockItemRepository;

    @Autowired
    private StockTransactionRepository transactionRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private fr.trollgun.optimiam.recipe.domain.RecipeRepository recipeRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private MockMvc mockMvc;
    private Product product;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        recipeRepository.deleteAll();
        stockItemRepository.deleteAll();
        transactionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        Category category = categoryRepository.save(Category.builder()
                .name("Fruits & Légumes")
                .build());

        product = productRepository.save(Product.builder()
                .name("Tomate")
                .defaultUnit(Unit.KG)
                .category(category)
                .averageShelfLifeDays(6)
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/stock/entries - Doit créer une entrée de stock")
    void shouldCreateStockEntry() throws Exception {
        CreateStockEntryRequest request = CreateStockEntryRequest.builder()
                .productId(product.getId())
                .quantity(new BigDecimal("1.500"))
                .location(Location.FRIDGE)
                .build();

        mockMvc.perform(post("/api/v1/stock/entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.quantity", is(1.5)))
                .andExpect(jsonPath("$.product.name", is("Tomate")))
                .andExpect(jsonPath("$.location", is("FRIDGE")));
    }

    @Test
    @DisplayName("GET /api/v1/stock - Doit lister les produits en stock")
    void shouldGetStock() throws Exception {
        stockItemRepository.save(StockItem.builder()
                .product(product)
                .quantity(new BigDecimal("2.000"))
                .unit(Unit.KG)
                .entryDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(5))
                .location(Location.FRIDGE)
                .status(StockStatus.AVAILABLE)
                .build());

        mockMvc.perform(get("/api/v1/stock")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].product.name", is("Tomate")));
    }

    @Test
    @DisplayName("POST /api/v1/stock/{id}/exits - Doit déduire la quantité de stock")
    void shouldExitStock() throws Exception {
        StockItem item = stockItemRepository.save(StockItem.builder()
                .product(product)
                .quantity(new BigDecimal("2.000"))
                .unit(Unit.KG)
                .entryDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(5))
                .location(Location.FRIDGE)
                .status(StockStatus.AVAILABLE)
                .build());

        StockExitRequest request = StockExitRequest.builder()
                .quantity(new BigDecimal("0.800"))
                .reason("Utilisé pour cuisiner")
                .build();

        mockMvc.perform(post("/api/v1/stock/" + item.getId() + "/exits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(1.2)));
    }

    @Test
    @DisplayName("POST /api/v1/stock/{id}/losses - Doit enregistrer une perte")
    void shouldRecordLoss() throws Exception {
        StockItem item = stockItemRepository.save(StockItem.builder()
                .product(product)
                .quantity(new BigDecimal("1.000"))
                .unit(Unit.KG)
                .entryDate(LocalDate.now())
                .expirationDate(LocalDate.now().minusDays(1))
                .location(Location.FRIDGE)
                .status(StockStatus.EXPIRED)
                .build());

        StockLossRequest request = StockLossRequest.builder()
                .quantity(new BigDecimal("1.000"))
                .lossReason(LossReason.EXPIRED)
                .comment("Tomates pourries")
                .build();

        mockMvc.perform(post("/api/v1/stock/" + item.getId() + "/losses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity", is(0.0)));
    }
}
