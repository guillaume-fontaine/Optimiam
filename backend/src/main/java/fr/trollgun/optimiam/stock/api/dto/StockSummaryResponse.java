package fr.trollgun.optimiam.stock.api.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class StockSummaryResponse {
    private long totalAvailableItems;
    private long expiringSoonItems;
    private long expiredItems;
    private BigDecimal totalLossesWeightKg;
    private long totalLossesCount;
}
