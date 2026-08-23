package fr.trollgun.optimiam.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.trollgun.optimiam.auth.api.dto.LoginRequest;
import fr.trollgun.optimiam.auth.api.dto.RegisterRequest;
import fr.trollgun.optimiam.hardware.api.dto.PrintLabelDto;
import fr.trollgun.optimiam.hardware.api.dto.SimulateWeightRequest;
import fr.trollgun.optimiam.nutrition.domain.Nutrition;
import fr.trollgun.optimiam.planning.api.dto.CreateMealPlanRequest;
import fr.trollgun.optimiam.planning.domain.MealPlanRepository;
import fr.trollgun.optimiam.planning.domain.MealType;
import fr.trollgun.optimiam.product.api.dto.CreateProductRequest;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.domain.Difficulty;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.recipe.domain.RecipeIngredient;
import fr.trollgun.optimiam.recipe.domain.RecipeRepository;
import fr.trollgun.optimiam.shopping.api.dto.GenerateShoppingListRequest;
import fr.trollgun.optimiam.shopping.domain.ShoppingListItemRepository;
import fr.trollgun.optimiam.shopping.domain.ShoppingListRepository;
import fr.trollgun.optimiam.stock.api.dto.CreateStockEntryRequest;
import fr.trollgun.optimiam.stock.domain.Location;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class DemoScenarioE2EIntegrationTest {

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

    @Autowired
    private fr.trollgun.optimiam.user.domain.UserRepository userRepository;

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
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Scénario Complet E2E : Inscription -> Stock -> Recommandations -> Planning -> Courses -> Cuisiner -> Sync")
    void shouldExecuteFullDemoUserStorySuccessfully() throws Exception {
        // ==========================================
        // 1. Inscription et Connexion utilisateur (JWT)
        // ==========================================
        RegisterRequest registerReq = RegisterRequest.builder()
                .username("Alice")
                .email("alice@optimiam.fr")
                .password("supersecret123")
                .build();

        MvcResult registerResult = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andReturn();

        String jwtToken = objectMapper.readTree(registerResult.getResponse().getContentAsString()).get("token").asText();
        assertThat(jwtToken).isNotBlank();

        // ==========================================
        // 2. Création de catégories et produits
        // ==========================================
        Category legumeCat = categoryRepository.save(Category.builder().name("Fruits & Légumes").color("#16a34a").build());
        Category feculentCat = categoryRepository.save(Category.builder().name("Féculents").color("#d97706").build());

        CreateProductRequest courgetteReq = CreateProductRequest.builder()
                .name("Courgette")
                .barcode("3228857000166")
                .defaultUnit(Unit.KG)
                .categoryId(legumeCat.getId())
                .averageShelfLifeDays(5)
                .build();

        MvcResult courgetteResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(courgetteReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID courgetteId = UUID.fromString(objectMapper.readTree(courgetteResult.getResponse().getContentAsString()).get("id").asText());

        CreateProductRequest rizReq = CreateProductRequest.builder()
                .name("Riz Basmati")
                .barcode("3123456789999")
                .defaultUnit(Unit.KG)
                .categoryId(feculentCat.getId())
                .averageShelfLifeDays(365)
                .build();

        MvcResult rizResult = mockMvc.perform(post("/api/v1/products")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(rizReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID rizId = UUID.fromString(objectMapper.readTree(rizResult.getResponse().getContentAsString()).get("id").asText());

        // ==========================================
        // 3. Simulation Balance Connectée & Entrée en Stock
        // ==========================================
        SimulateWeightRequest scaleReq = SimulateWeightRequest.builder()
                .weight(new BigDecimal("1.000"))
                .unit(Unit.KG)
                .stable(true)
                .build();

        mockMvc.perform(post("/api/v1/hardware/scale/simulate")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(scaleReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weight", is(1.000)));

        // Créer entrée en stock pour la courgette (DLC courte: dans 2 jours)
        LocalDate today = LocalDate.now();
        CreateStockEntryRequest stockEntry = CreateStockEntryRequest.builder()
                .productId(courgetteId)
                .quantity(new BigDecimal("1.000"))
                .unit(Unit.KG)
                .location(Location.FRIDGE)
                .entryDate(today)
                .expirationDate(today.plusDays(2))
                .build();

        MvcResult stockResult = mockMvc.perform(post("/api/v1/stock/entries")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(stockEntry)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status", is("EXPIRING_SOON")))
                .andExpect(jsonPath("$.daysUntilExpiration", is(2)))
                .andReturn();

        UUID stockItemId = UUID.fromString(objectMapper.readTree(stockResult.getResponse().getContentAsString()).get("id").asText());

        // ==========================================
        // 4. Imprimer une étiquette de traçabilité thermique
        // ==========================================
        PrintLabelDto printReq = PrintLabelDto.builder()
                .productName("Courgette")
                .barcode("3228857000166")
                .quantityWithUnit("1.000 kg")
                .location("FRIDGE")
                .entryDate(today)
                .expirationDate(today.plusDays(2))
                .build();

        mockMvc.perform(post("/api/v1/hardware/printer/print-label")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(printReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)))
                .andExpect(jsonPath("$.labelContent", containsString("OPTIMIAM - ÉTIQUETTE STOCK")));

        // ==========================================
        // 5. Dashboard : vérification des alertes anti-gaspillage
        // ==========================================
        mockMvc.perform(get("/api/v1/stock/summary")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAvailableItems", is(1)))
                .andExpect(jsonPath("$.expiringSoonItems", is(1)));

        mockMvc.perform(get("/api/v1/stock/expiring")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // ==========================================
        // 6. Recette & Moteur de Recommandation
        // ==========================================
        Recipe poelee = Recipe.builder()
                .name("Poêlée de Courgettes et Riz")
                .description("Délicieuse poêlée anti-gaspillage")
                .preparationTimeMinutes(10)
                .cookingTimeMinutes(15)
                .servings(2)
                .difficulty(Difficulty.EASY)
                .tags(new java.util.HashSet<>(java.util.List.of("Anti-gaspi")))
                .nutrition(Nutrition.builder()
                        .calories(new BigDecimal("250"))
                        .protein(new BigDecimal("5.0"))
                        .carbohydrates(new BigDecimal("45.0"))
                        .fat(new BigDecimal("3.0"))
                        .build())
                .build();

        poelee.addIngredient(RecipeIngredient.builder()
                .product(productRepository.findById(courgetteId).orElseThrow())
                .quantity(new BigDecimal("0.500"))
                .unit(Unit.KG)
                .optional(false)
                .build());

        poelee.addIngredient(RecipeIngredient.builder()
                .product(productRepository.findById(rizId).orElseThrow())
                .quantity(new BigDecimal("0.200"))
                .unit(Unit.KG)
                .optional(false)
                .build());

        recipeRepository.save(poelee);

        // Appel des recommandations
        MvcResult recoResult = mockMvc.perform(post("/api/v1/recommendations")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].recipe.name", is("Poêlée de Courgettes et Riz")))
                .andExpect(jsonPath("$[0].matchPercentage", is(50)))
                .andExpect(jsonPath("$[0].missingIngredients", hasSize(1)))
                .andReturn();

        // ==========================================
        // 7. Planification du Repas
        // ==========================================
        CreateMealPlanRequest mealReq = CreateMealPlanRequest.builder()
                .recipeId(poelee.getId())
                .date(today)
                .mealType(MealType.DINNER)
                .servings(2)
                .build();

        MvcResult planResult = mockMvc.perform(post("/api/v1/planning")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mealReq)))
                .andExpect(status().isCreated())
                .andReturn();

        UUID mealPlanId = UUID.fromString(objectMapper.readTree(planResult.getResponse().getContentAsString()).get("id").asText());

        // ==========================================
        // 8. Génération Liste de Courses Automatique
        // ==========================================
        GenerateShoppingListRequest shopReq = GenerateShoppingListRequest.builder()
                .startDate(today)
                .endDate(today.plusDays(3))
                .build();

        MvcResult shopResult = mockMvc.perform(post("/api/v1/shopping-lists/generate")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(shopReq)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].product.name", is("Riz Basmati")))
                .andReturn();

        // ==========================================
        // 9. Cuisiner le Repas (Déduction automatique de stock)
        // ==========================================
        mockMvc.perform(post("/api/v1/planning/" + mealPlanId + "/cook")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("COOKED")));

        // Vérifier que le stock de courgettes est passé de 1.000 à 0.500 kg
        mockMvc.perform(get("/api/v1/stock")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].quantity", is(0.5)));

        // ==========================================
        // 10. Synchronisation Hors-Ligne (PWA Offline Sync)
        // ==========================================
        SyncOperationDto offlineOp = SyncOperationDto.builder()
                .operationId(UUID.randomUUID())
                .type(SyncOperationType.EXIT_STOCK)
                .entityId(stockItemId)
                .payload(Map.of("quantity", 0.5, "reason", "Dégustation crue"))
                .build();

        SyncBatchRequest syncBatch = SyncBatchRequest.builder()
                .clientId("pwa-demo-client")
                .operations(List.of(offlineOp))
                .build();

        mockMvc.perform(post("/api/v1/sync/batch")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(syncBatch)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.syncedCount", is(1)))
                .andExpect(jsonPath("$.conflictCount", is(0)));

        // Le stock est maintenant entièrement consommé (0.000 kg restant)
        mockMvc.perform(get("/api/v1/stock/summary")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAvailableItems", is(0)));
    }
}
