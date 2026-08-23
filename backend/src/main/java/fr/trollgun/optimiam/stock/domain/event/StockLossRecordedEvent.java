package fr.trollgun.optimiam.stock.domain.event;

import fr.trollgun.optimiam.common.event.DomainEvent;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.transaction.domain.LossReason;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
public class StockLossRecordedEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
    private final UUID stockItemId;
    private final Product product;
    private final BigDecimal quantity;
    private final Unit unit;
    private final LossReason lossReason;
    private final String comment;

    public StockLossRecordedEvent(UUID stockItemId, Product product, BigDecimal quantity, Unit unit, LossReason lossReason, String comment) {
        this.stockItemId = stockItemId;
        this.product = product;
        this.quantity = quantity;
        this.unit = unit;
        this.lossReason = lossReason;
        this.comment = comment;
    }

    @Override
    public String getEventType() {
        return "StockLossRecorded";
    }
}
