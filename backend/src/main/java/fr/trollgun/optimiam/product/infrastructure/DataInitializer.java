package fr.trollgun.optimiam.product.infrastructure;

import fr.trollgun.optimiam.nutrition.domain.Nutrition;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.domain.Difficulty;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.recipe.domain.RecipeIngredient;
import fr.trollgun.optimiam.recipe.domain.RecipeRepository;
import fr.trollgun.optimiam.recipe.domain.RecipeStep;
import fr.trollgun.optimiam.stock.domain.Location;
import fr.trollgun.optimiam.stock.domain.StockItem;
import fr.trollgun.optimiam.stock.domain.StockItemRepository;
import fr.trollgun.optimiam.stock.domain.StockStatus;
import fr.trollgun.optimiam.transaction.domain.StockTransaction;
import fr.trollgun.optimiam.transaction.domain.StockTransactionRepository;
import fr.trollgun.optimiam.transaction.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;
    private final StockTransactionRepository transactionRepository;
    private final RecipeRepository recipeRepository;
    private final fr.trollgun.optimiam.planning.domain.MealPlanRepository mealPlanRepository;
    private final fr.trollgun.optimiam.user.domain.UserRepository userRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            log.info("Création des comptes utilisateurs de démonstration (demo@optimiam.fr, admin@optimiam.fr)...");
            userRepository.save(fr.trollgun.optimiam.user.domain.User.builder()
                    .email("demo@optimiam.fr")
                    .username("Utilisateur Démo")
                    .password(passwordEncoder.encode("demo123"))
                    .role(fr.trollgun.optimiam.user.domain.Role.ROLE_USER)
                    .maxPrepTimeMinutes(30)
                    .build());

            userRepository.save(fr.trollgun.optimiam.user.domain.User.builder()
                    .email("admin@optimiam.fr")
                    .username("Administrateur")
                    .password(passwordEncoder.encode("admin123"))
                    .role(fr.trollgun.optimiam.user.domain.Role.ROLE_ADMIN)
                    .maxPrepTimeMinutes(45)
                    .build());
        }

        if (categoryRepository.count() == 0) {
            log.info("Initialisation des catégories, produits, stocks, recettes et planning par défaut...");

            Category fruitsLegumes = categoryRepository.save(Category.builder()
                    .name("Fruits & Légumes")
                    .icon("eco")
                    .color("#16a34a")
                    .description("Fruits et légumes frais")
                    .build());

            Category viandesPoissons = categoryRepository.save(Category.builder()
                    .name("Viandes & Poissons")
                    .icon("set_meal")
                    .color("#dc2626")
                    .description("Viandes, volailles, poissons et fruits de mer")
                    .build());

            Category produitsLaitiers = categoryRepository.save(Category.builder()
                    .name("Produits Laitiers & Œufs")
                    .icon("egg")
                    .color("#eab308")
                    .description("Laits, crèmes, fromages, beurres et œufs")
                    .build());

            Category feculents = categoryRepository.save(Category.builder()
                    .name("Féculents & Céréales")
                    .icon("grain")
                    .color("#d97706")
                    .description("Riz, pâtes, farines, légumes secs, pain")
                    .build());

            Category epicerie = categoryRepository.save(Category.builder()
                    .name("Épicerie & Condiments")
                    .icon("kitchen")
                    .color("#9333ea")
                    .description("Huiles, épices, sauces, conserves")
                    .build());

            Category boissons = categoryRepository.save(Category.builder()
                    .name("Boissons")
                    .icon("local_drink")
                    .color("#0284c7")
                    .description("Jus, eaux, laits végétaux")
                    .build());

            // Produits initiaux
            Product tomate = productRepository.save(Product.builder().name("Tomate").barcode("300001").defaultUnit(Unit.KG).defaultLocation(Location.FRIDGE).category(fruitsLegumes).averageShelfLifeDays(6).build());
            Product courgette = productRepository.save(Product.builder().name("Courgette").barcode("300002").defaultUnit(Unit.KG).defaultLocation(Location.FRIDGE).category(fruitsLegumes).averageShelfLifeDays(7).build());
            Product oignon = productRepository.save(Product.builder().name("Oignon jaune").barcode("300003").defaultUnit(Unit.KG).defaultLocation(Location.PANTRY).category(fruitsLegumes).averageShelfLifeDays(30).build());
            Product ail = productRepository.save(Product.builder().name("Ail").barcode("300004").defaultUnit(Unit.PIECE).defaultLocation(Location.PANTRY).category(fruitsLegumes).averageShelfLifeDays(60).build());
            Product salade = productRepository.save(Product.builder().name("Salade verte").barcode("300005").defaultUnit(Unit.PIECE).defaultLocation(Location.FRIDGE).category(fruitsLegumes).averageShelfLifeDays(4).build());
            Product pomme = productRepository.save(Product.builder().name("Pomme").barcode("300006").defaultUnit(Unit.KG).defaultLocation(Location.PANTRY).category(fruitsLegumes).averageShelfLifeDays(15).build());

            Product oeufs = productRepository.save(Product.builder().name("Œufs frais").barcode("300010").defaultUnit(Unit.PIECE).defaultLocation(Location.FRIDGE).category(produitsLaitiers).averageShelfLifeDays(21).build());
            Product fromage = productRepository.save(Product.builder().name("Fromage râpé (Emmental)").barcode("300011").defaultUnit(Unit.G).defaultLocation(Location.FRIDGE).category(produitsLaitiers).averageShelfLifeDays(25).build());
            Product lait = productRepository.save(Product.builder().name("Lait demi-écrémé").barcode("300012").defaultUnit(Unit.L).defaultLocation(Location.FRIDGE).category(produitsLaitiers).averageShelfLifeDays(60).build());
            Product beurre = productRepository.save(Product.builder().name("Beurre doux").barcode("300013").defaultUnit(Unit.G).defaultLocation(Location.FRIDGE).category(produitsLaitiers).averageShelfLifeDays(45).build());

            Product poulet = productRepository.save(Product.builder().name("Blanc de poulet").barcode("300020").defaultUnit(Unit.KG).defaultLocation(Location.FRIDGE).category(viandesPoissons).averageShelfLifeDays(5).build());
            Product saumon = productRepository.save(Product.builder().name("Pavé de saumon").barcode("300021").defaultUnit(Unit.G).defaultLocation(Location.FRIDGE).category(viandesPoissons).averageShelfLifeDays(3).build());

            Product riz = productRepository.save(Product.builder().name("Riz basmati").barcode("300030").defaultUnit(Unit.KG).defaultLocation(Location.PANTRY).category(feculents).averageShelfLifeDays(365).build());
            Product pates = productRepository.save(Product.builder().name("Pâtes Penne").barcode("300031").defaultUnit(Unit.G).defaultLocation(Location.PANTRY).category(feculents).averageShelfLifeDays(365).build());
            Product farine = productRepository.save(Product.builder().name("Farine de blé").barcode("300032").defaultUnit(Unit.KG).defaultLocation(Location.PANTRY).category(feculents).averageShelfLifeDays(180).build());

            Product huile = productRepository.save(Product.builder().name("Huile d'olive").barcode("300040").defaultUnit(Unit.ML).defaultLocation(Location.PANTRY).category(epicerie).averageShelfLifeDays(365).build());
            Product sel = productRepository.save(Product.builder().name("Sel fin").barcode("300041").defaultUnit(Unit.G).defaultLocation(Location.PANTRY).category(epicerie).averageShelfLifeDays(730).build());
            Product poivre = productRepository.save(Product.builder().name("Poivre noir moulu").barcode("300042").defaultUnit(Unit.G).defaultLocation(Location.PANTRY).category(epicerie).averageShelfLifeDays(730).build());

            // Initialisation de stocks de test conformes à OptiMiam.md section 19
            LocalDate today = LocalDate.now();

            createInitialStock(tomate, new BigDecimal("1.000"), Unit.KG, today.minusDays(5), today.plusDays(1), Location.FRIDGE);
            createInitialStock(courgette, new BigDecimal("0.500"), Unit.KG, today.minusDays(4), today.plusDays(2), Location.FRIDGE);
            createInitialStock(oeufs, new BigDecimal("6"), Unit.PIECE, today.minusDays(2), today.plusDays(15), Location.FRIDGE);
            createInitialStock(fromage, new BigDecimal("200"), Unit.G, today.minusDays(3), today.plusDays(18), Location.FRIDGE);
            createInitialStock(oignon, new BigDecimal("1.500"), Unit.KG, today.minusDays(10), today.plusDays(20), Location.PANTRY);
            createInitialStock(riz, new BigDecimal("1.000"), Unit.KG, today.minusDays(20), today.plusDays(300), Location.PANTRY);

            // Initialisation des recettes démo
            createInitialRecipes(tomate, courgette, oignon, ail, oeufs, fromage, salade, poulet, riz, pates, huile, sel, poivre, beurre);

            log.info("Catégories, produits, stocks et recettes créés avec succès !");
        }
    }

    private void createInitialStock(Product product, BigDecimal quantity, Unit unit, LocalDate entryDate, LocalDate expirationDate, Location location) {
        StockItem item = stockItemRepository.save(StockItem.builder()
                .product(product)
                .quantity(quantity)
                .unit(unit)
                .entryDate(entryDate)
                .expirationDate(expirationDate)
                .location(location)
                .status(StockStatus.AVAILABLE)
                .build());

        transactionRepository.save(StockTransaction.builder()
                .stockItemId(item.getId())
                .product(product)
                .type(TransactionType.ENTRY)
                .quantity(quantity)
                .unit(unit)
                .reason("Stock initial démo")
                .build());
    }

    private void createInitialRecipes(Product tomate, Product courgette, Product oignon, Product ail,
                                      Product oeufs, Product fromage, Product salade, Product poulet,
                                      Product riz, Product pates, Product huile, Product sel, Product poivre, Product beurre) {
        // 1. Ratatouille provençale
        Recipe ratatouille = Recipe.builder()
                .name("Ratatouille provençale")
                .description("Un grand classique méditerranéen mijoté, idéal pour utiliser les courgettes et tomates mûres.")
                .preparationTimeMinutes(20)
                .cookingTimeMinutes(35)
                .difficulty(Difficulty.EASY)
                .servings(4)
                .tags(Set.of("Végétarien", "Anti-gaspi", "Méditerranéen", "Plat chaud"))
                .nutrition(Nutrition.builder()
                        .calories(new BigDecimal("185"))
                        .protein(new BigDecimal("4.2"))
                        .carbohydrates(new BigDecimal("16.5"))
                        .fat(new BigDecimal("9.8"))
                        .fiber(new BigDecimal("5.5"))
                        .salt(new BigDecimal("1.2"))
                        .build())
                .build();

        ratatouille.addIngredient(RecipeIngredient.builder().product(tomate).quantity(new BigDecimal("0.800")).unit(Unit.KG).build());
        ratatouille.addIngredient(RecipeIngredient.builder().product(courgette).quantity(new BigDecimal("0.500")).unit(Unit.KG).build());
        ratatouille.addIngredient(RecipeIngredient.builder().product(oignon).quantity(new BigDecimal("0.200")).unit(Unit.KG).build());
        ratatouille.addIngredient(RecipeIngredient.builder().product(ail).quantity(new BigDecimal("2")).unit(Unit.PIECE).build());
        ratatouille.addIngredient(RecipeIngredient.builder().product(huile).quantity(new BigDecimal("30")).unit(Unit.ML).build());
        ratatouille.addIngredient(RecipeIngredient.builder().product(sel).quantity(new BigDecimal("5")).unit(Unit.G).optional(true).build());

        ratatouille.addStep(RecipeStep.builder().stepNumber(1).instruction("Laver les courgettes et tomates, puis les couper en dés réguliers.").durationMinutes(10).build());
        ratatouille.addStep(RecipeStep.builder().stepNumber(2).instruction("Émincer l'oignon et hacher l'ail. Les faire suer dans une sauteuse avec l'huile d'olive.").durationMinutes(5).build());
        ratatouille.addStep(RecipeStep.builder().stepNumber(3).instruction("Ajouter les courgettes et cuire 10 minutes à feu moyen.").durationMinutes(10).build());
        ratatouille.addStep(RecipeStep.builder().stepNumber(4).instruction("Incorporer les tomates, saler, poivrer et laisser mijoter à feu doux à couvert.").durationMinutes(20).build());

        Recipe savedRatatouille = recipeRepository.save(ratatouille);

        // 2. Omelette aux légumes & fromage
        Recipe omelette = Recipe.builder()
                .name("Omelette aux légumes & fromage")
                .description("Une omelette moelleuse et gourmande prête en quelques minutes.")
                .preparationTimeMinutes(10)
                .cookingTimeMinutes(10)
                .difficulty(Difficulty.EASY)
                .servings(2)
                .tags(Set.of("Rapide", "Anti-gaspi", "Protéiné", "Express"))
                .nutrition(Nutrition.builder()
                        .calories(new BigDecimal("310"))
                        .protein(new BigDecimal("21.5"))
                        .carbohydrates(new BigDecimal("4.0"))
                        .fat(new BigDecimal("23.0"))
                        .fiber(new BigDecimal("2.1"))
                        .salt(new BigDecimal("1.5"))
                        .build())
                .build();

        omelette.addIngredient(RecipeIngredient.builder().product(oeufs).quantity(new BigDecimal("4")).unit(Unit.PIECE).build());
        omelette.addIngredient(RecipeIngredient.builder().product(courgette).quantity(new BigDecimal("0.150")).unit(Unit.KG).build());
        omelette.addIngredient(RecipeIngredient.builder().product(tomate).quantity(new BigDecimal("0.100")).unit(Unit.KG).build());
        omelette.addIngredient(RecipeIngredient.builder().product(fromage).quantity(new BigDecimal("50")).unit(Unit.G).build());
        omelette.addIngredient(RecipeIngredient.builder().product(beurre).quantity(new BigDecimal("15")).unit(Unit.G).build());

        omelette.addStep(RecipeStep.builder().stepNumber(1).instruction("Couper la courgette et la tomate en petits dés et les faire sauter avec une noisette de beurre.").durationMinutes(5).build());
        omelette.addStep(RecipeStep.builder().stepNumber(2).instruction("Battre les œufs en omelette avec sel et poivre, puis verser dans la poêle chaude.").durationMinutes(2).build());
        omelette.addStep(RecipeStep.builder().stepNumber(3).instruction("Parsemer de fromage râpé et replier l'omelette à la consistance souhaitée.").durationMinutes(3).build());

        Recipe savedOmelette = recipeRepository.save(omelette);

        // 3. Salade fraîcheur tomate & emmental
        Recipe saladeTomate = Recipe.builder()
                .name("Salade fraîcheur tomate & emmental")
                .description("Une salade croquante et rapide pour un déjeuner léger.")
                .preparationTimeMinutes(10)
                .cookingTimeMinutes(0)
                .difficulty(Difficulty.EASY)
                .servings(2)
                .tags(Set.of("Sans cuisson", "Fraîcheur", "Rapide", "Végétarien"))
                .nutrition(Nutrition.builder()
                        .calories(new BigDecimal("165"))
                        .protein(new BigDecimal("6.5"))
                        .carbohydrates(new BigDecimal("7.0"))
                        .fat(new BigDecimal("12.0"))
                        .fiber(new BigDecimal("3.0"))
                        .salt(new BigDecimal("0.8"))
                        .build())
                .build();

        saladeTomate.addIngredient(RecipeIngredient.builder().product(tomate).quantity(new BigDecimal("0.400")).unit(Unit.KG).build());
        saladeTomate.addIngredient(RecipeIngredient.builder().product(salade).quantity(new BigDecimal("1")).unit(Unit.PIECE).build());
        saladeTomate.addIngredient(RecipeIngredient.builder().product(fromage).quantity(new BigDecimal("40")).unit(Unit.G).build());
        saladeTomate.addIngredient(RecipeIngredient.builder().product(huile).quantity(new BigDecimal("15")).unit(Unit.ML).build());

        saladeTomate.addStep(RecipeStep.builder().stepNumber(1).instruction("Laver et essorer la salade verte, trancher les tomates en quartiers.").durationMinutes(5).build());
        saladeTomate.addStep(RecipeStep.builder().stepNumber(2).instruction("Dresser dans les assiettes, ajouter le fromage râpé et assaisonner avec l'huile d'olive.").durationMinutes(5).build());

        Recipe savedSalade = recipeRepository.save(saladeTomate);

        // Planning des repas initiaux
        LocalDate today = LocalDate.now();
        mealPlanRepository.save(fr.trollgun.optimiam.planning.domain.MealPlan.builder()
                .date(today)
                .mealType(fr.trollgun.optimiam.planning.domain.MealType.LUNCH)
                .recipe(savedSalade)
                .servings(2)
                .status(fr.trollgun.optimiam.planning.domain.MealPlanStatus.PLANNED)
                .notes("Déjeuner rapide midi")
                .build());

        mealPlanRepository.save(fr.trollgun.optimiam.planning.domain.MealPlan.builder()
                .date(today)
                .mealType(fr.trollgun.optimiam.planning.domain.MealType.DINNER)
                .recipe(savedRatatouille)
                .servings(4)
                .status(fr.trollgun.optimiam.planning.domain.MealPlanStatus.PLANNED)
                .notes("Dîner convivial anti-gaspi")
                .build());

        mealPlanRepository.save(fr.trollgun.optimiam.planning.domain.MealPlan.builder()
                .date(today.plusDays(1))
                .mealType(fr.trollgun.optimiam.planning.domain.MealType.LUNCH)
                .recipe(savedOmelette)
                .servings(2)
                .status(fr.trollgun.optimiam.planning.domain.MealPlanStatus.PLANNED)
                .notes("Omelette express")
                .build());
    }
}
