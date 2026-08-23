package fr.trollgun.optimiam.hardware.infrastructure;

import fr.trollgun.optimiam.hardware.domain.ScanResult;
import fr.trollgun.optimiam.hardware.domain.ScannerDriver;
import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.stock.api.dto.StockItemResponse;
import fr.trollgun.optimiam.stock.domain.StockItem;
import fr.trollgun.optimiam.stock.domain.StockItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimulatedScannerDriver implements ScannerDriver {

    private final ProductRepository productRepository;
    private final StockItemRepository stockItemRepository;

    @Override
    public ScanResult scanBarcode(String rawBarcode) {
        if (rawBarcode == null || rawBarcode.isBlank()) {
            return ScanResult.builder()
                    .rawBarcode("")
                    .productFound(false)
                    .message("Code-barres vide")
                    .build();
        }

        String cleaned = rawBarcode.trim();

        // Recherche par code-barre exact ou par correspondance de nom pour le simulateur
        Optional<Product> matched = productRepository.findAll().stream()
                .filter(p -> cleaned.equalsIgnoreCase(p.getBarcode()) || p.getName().equalsIgnoreCase(cleaned))
                .findFirst();

        if (matched.isEmpty()) {
            log.warn("Scanner simulé : aucun produit associé au code '{}'", cleaned);
            return ScanResult.builder()
                    .rawBarcode(cleaned)
                    .productFound(false)
                    .message("Produit non référencé dans le catalogue")
                    .build();
        }

        Product product = matched.get();
        List<StockItem> stockItems = stockItemRepository.findByQuantityGreaterThanOrderByExpirationDateAsc(BigDecimal.ZERO).stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .collect(Collectors.toList());

        List<StockItemResponse> stockResponses = stockItems.stream()
                .map(item -> StockItemResponse.from(item, LocalDate.now()))
                .collect(Collectors.toList());

        log.info("Scanner simulé : produit identifié '{}' [id={}], {} lots en stock",
                product.getName(), product.getId(), stockResponses.size());

        return ScanResult.builder()
                .rawBarcode(cleaned)
                .productFound(true)
                .matchedProduct(ProductResponse.from(product))
                .matchingStockItems(stockResponses)
                .message("Produit identifié avec succès")
                .build();
    }
}
