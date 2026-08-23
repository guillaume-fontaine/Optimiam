package fr.trollgun.optimiam.stock.domain.event;

import fr.trollgun.optimiam.common.event.DomainEvent;
import fr.trollgun.optimiam.stock.domain.StockItem;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class StockEntryCreatedEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
    private final StockItem stockItem;

    public StockEntryCreatedEvent(StockItem stockItem) {
        this.stockItem = stockItem;
    }

    @Override
    public String getEventType() {
        return "StockEntryCreated";
    }
}
