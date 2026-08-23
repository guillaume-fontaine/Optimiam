package fr.trollgun.optimiam.hardware.domain;

import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.stock.api.dto.StockItemResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanResult {
    private String rawBarcode;
    private boolean productFound;
    private ProductResponse matchedProduct;
    private List<StockItemResponse> matchingStockItems;
    @Builder.Default
    private Instant scannedAt = Instant.now();
    private String message;
}
