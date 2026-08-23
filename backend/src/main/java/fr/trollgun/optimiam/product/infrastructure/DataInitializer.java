package fr.trollgun.optimiam.product.infrastructure;

import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.count() == 0) {
            log.info("Initialisation des catégories et produits par défaut pour OptiMiam...");

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
            productRepository.saveAll(List.of(
                    Product.builder().name("Tomate").barcode("300001").defaultUnit(Unit.KG).category(fruitsLegumes).averageShelfLifeDays(6).build(),
                    Product.builder().name("Courgette").barcode("300002").defaultUnit(Unit.KG).category(fruitsLegumes).averageShelfLifeDays(7).build(),
                    Product.builder().name("Oignon jaune").barcode("300003").defaultUnit(Unit.KG).category(fruitsLegumes).averageShelfLifeDays(30).build(),
                    Product.builder().name("Ail").barcode("300004").defaultUnit(Unit.PIECE).category(fruitsLegumes).averageShelfLifeDays(60).build(),
                    Product.builder().name("Salade verte").barcode("300005").defaultUnit(Unit.PIECE).category(fruitsLegumes).averageShelfLifeDays(4).build(),
                    Product.builder().name("Pomme").barcode("300006").defaultUnit(Unit.KG).category(fruitsLegumes).averageShelfLifeDays(15).build(),

                    Product.builder().name("Œufs frais").barcode("300010").defaultUnit(Unit.PIECE).category(produitsLaitiers).averageShelfLifeDays(21).build(),
                    Product.builder().name("Fromage râpé (Emmental)").barcode("300011").defaultUnit(Unit.G).category(produitsLaitiers).averageShelfLifeDays(25).build(),
                    Product.builder().name("Lait demi-écrémé").barcode("300012").defaultUnit(Unit.L).category(produitsLaitiers).averageShelfLifeDays(60).build(),
                    Product.builder().name("Beurre doux").barcode("300013").defaultUnit(Unit.G).category(produitsLaitiers).averageShelfLifeDays(45).build(),

                    Product.builder().name("Blanc de poulet").barcode("300020").defaultUnit(Unit.KG).category(viandesPoissons).averageShelfLifeDays(5).build(),
                    Product.builder().name("Pavé de saumon").barcode("300021").defaultUnit(Unit.G).category(viandesPoissons).averageShelfLifeDays(3).build(),

                    Product.builder().name("Riz basmati").barcode("300030").defaultUnit(Unit.KG).category(feculents).averageShelfLifeDays(365).build(),
                    Product.builder().name("Pâtes Penne").barcode("300031").defaultUnit(Unit.G).category(feculents).averageShelfLifeDays(365).build(),
                    Product.builder().name("Farine de blé").barcode("300032").defaultUnit(Unit.KG).category(feculents).averageShelfLifeDays(180).build(),

                    Product.builder().name("Huile d'olive").barcode("300040").defaultUnit(Unit.ML).category(epicerie).averageShelfLifeDays(365).build(),
                    Product.builder().name("Sel fin").barcode("300041").defaultUnit(Unit.G).category(epicerie).averageShelfLifeDays(730).build(),
                    Product.builder().name("Poivre noir moulu").barcode("300042").defaultUnit(Unit.G).category(epicerie).averageShelfLifeDays(730).build()
            ));

            log.info("Données initiales créées avec succès !");
        }
    }
}
