package fr.trollgun.optimiam.transaction.api.dto;

import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.transaction.domain.LossReason;
import fr.trollgun.optimiam.transaction.domain.StockTransaction;
import fr.trollgun.optimiam.transaction.domain.TransactionType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class StockTransactionResponse {
    private UUID id;
    private UUID stockItemId;
    private ProductResponse product;
    private TransactionType type;
    private String typeLabel;
    private BigDecimal quantity;
    private Unit unit;
    private String unitSymbol;
    private LossReason lossReason;
    private String lossReasonLabel;
    private String reason;
    private String deviceId;
    private Instant timestamp;

    public static StockTransactionResponse from(StockTransaction transaction) {
        if (transaction == null) return null;
        return StockTransactionResponse.builder()
                .id(transaction.getId())
                .stockItemId(transaction.getStockItemId())
                .product(ProductResponse.from(transaction.getProduct()))
                .type(transaction.getType())
                .typeLabel(transaction.getType() != null ? transaction.getType().getLabel() : null)
                .quantity(transaction.getQuantity())
                .unit(transaction.getUnit())
                .unitSymbol(transaction.getUnit() != null ? transaction.getUnit().getSymbol() : null)
                .lossReason(transaction.getLossReason())
                .lossReasonLabel(transaction.getLossReason() != null ? transaction.getLossReason().getLabel() : null)
                .reason(transaction.getReason())
                .deviceId(transaction.getDeviceId())
                .timestamp(transaction.getTimestamp())
                .build();
    }
}
