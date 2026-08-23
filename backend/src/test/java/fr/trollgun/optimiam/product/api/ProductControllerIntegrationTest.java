package fr.trollgun.optimiam.product.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.trollgun.optimiam.product.api.dto.CreateProductRequest;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
class ProductControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private fr.trollgun.optimiam.stock.domain.StockItemRepository stockItemRepository;

    @Autowired
    private fr.trollgun.optimiam.transaction.domain.StockTransactionRepository transactionRepository;

    @Autowired
    private fr.trollgun.optimiam.recipe.domain.RecipeRepository recipeRepository;

    @Autowired
    private fr.trollgun.optimiam.planning.domain.MealPlanRepository mealPlanRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());

    private MockMvc mockMvc;
    private Category category;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        mealPlanRepository.deleteAll();
        recipeRepository.deleteAll();
        transactionRepository.deleteAll();
        stockItemRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        category = categoryRepository.save(Category.builder()
                .name("Fruits & Légumes")
                .icon("eco")
                .color("#16a34a")
                .build());
    }

    @Test
    @DisplayName("GET /api/v1/products - Doit retourner la liste paginée des produits")
    void shouldReturnPaginatedProducts() throws Exception {
        productRepository.save(Product.builder()
                .name("Tomate Ronde")
                .barcode("123456")
                .defaultUnit(Unit.KG)
                .category(category)
                .averageShelfLifeDays(6)
                .build());

        mockMvc.perform(get("/api/v1/products")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("Tomate Ronde")))
                .andExpect(jsonPath("$.content[0].unitLabel", is("Kilogramme")))
                .andExpect(jsonPath("$.totalElements", is(1)));
    }

    @Test
    @DisplayName("POST /api/v1/products - Doit créer un nouveau produit avec succès")
    void shouldCreateProduct() throws Exception {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Courgette Verte")
                .barcode("654321")
                .defaultUnit(Unit.KG)
                .categoryId(category.getId())
                .averageShelfLifeDays(8)
                .build();

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.name", is("Courgette Verte")))
                .andExpect(jsonPath("$.category.name", is("Fruits & Légumes")));
    }

    @Test
    @DisplayName("DELETE /api/v1/products/{id} - Doit effectuer un soft delete")
    void shouldSoftDeleteProduct() throws Exception {
        Product product = productRepository.save(Product.builder()
                .name("Produit à supprimer")
                .defaultUnit(Unit.PIECE)
                .category(category)
                .build());

        mockMvc.perform(delete("/api/v1/products/" + product.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/products/" + product.getId()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code", is("PRODUCT_NOT_FOUND")));
    }
}
