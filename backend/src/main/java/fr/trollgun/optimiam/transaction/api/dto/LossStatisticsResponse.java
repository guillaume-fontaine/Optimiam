package fr.trollgun.optimiam.transaction.api.dto;

import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.transaction.domain.LossReason;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class LossStatisticsResponse {
    private BigDecimal totalLossWeightKg;
    private long totalLossOperations;
    private Map<LossReason, BigDecimal> lossesByReason;
    private List<ProductLossItem> topLostProducts;

    @Getter
    @Builder
    public static class ProductLossItem {
        private ProductResponse product;
        private BigDecimal totalQuantity;
        private String unitSymbol;
        private long occurrences;
    }
}
