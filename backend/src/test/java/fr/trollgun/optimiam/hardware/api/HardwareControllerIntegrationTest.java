package fr.trollgun.optimiam.hardware.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.trollgun.optimiam.hardware.api.dto.PrintLabelDto;
import fr.trollgun.optimiam.hardware.api.dto.ScanBarcodeDto;
import fr.trollgun.optimiam.hardware.api.dto.SimulateWeightRequest;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class HardwareControllerIntegrationTest {

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
        product = productRepository.save(Product.builder()
                .name("Tomate")
                .barcode("3017620422003")
                .defaultUnit(Unit.KG)
                .category(category)
                .build());
    }

    @Test
    @DisplayName("GET & POST /api/v1/hardware/scale - Lecture et simulation de balance")
    void shouldInteractWithScale() throws Exception {
        // 1. Lire la mesure
        mockMvc.perform(get("/api/v1/hardware/scale/measure"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight", notNullValue()));

        // 2. Simuler un nouveau poids
        SimulateWeightRequest simReq = SimulateWeightRequest.builder()
                .weight(new BigDecimal("1.250"))
                .unit(Unit.KG)
                .stable(true)
                .build();

        mockMvc.perform(post("/api/v1/hardware/scale/simulate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(simReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight", is(1.250)))
                .andExpect(jsonPath("$.stable", is(true)));

        // 3. Tarer
        mockMvc.perform(post("/api/v1/hardware/scale/tare"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight", is(0.0)));
    }

    @Test
    @DisplayName("POST & GET /api/v1/hardware/printer - Impression d'étiquette thermique")
    void shouldPrintLabel() throws Exception {
        PrintLabelDto dto = PrintLabelDto.builder()
                .productName("Tomate")
                .barcode("3017620422003")
                .quantityWithUnit("1.250 kg")
                .location("FRIDGE")
                .entryDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(7))
                .build();

        mockMvc.perform(post("/api/v1/hardware/printer/print-label")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productName", is("Tomate")))
                .andExpect(jsonPath("$.labelContent", containsString("OPTIMIAM - ÉTIQUETTE STOCK")));

        mockMvc.perform(get("/api/v1/hardware/printer/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", not(empty())));
    }

    @Test
    @DisplayName("POST /api/v1/hardware/scanner/scan - Scan d'un code-barres")
    void shouldScanBarcode() throws Exception {
        ScanBarcodeDto dto = ScanBarcodeDto.builder()
                .barcode("3017620422003")
                .build();

        mockMvc.perform(post("/api/v1/hardware/scanner/scan")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.productFound", is(true)))
                .andExpect(jsonPath("$.matchedProduct.name", is("Tomate")));
    }
}
