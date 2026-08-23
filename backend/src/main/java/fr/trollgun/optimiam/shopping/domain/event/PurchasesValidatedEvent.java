package fr.trollgun.optimiam.shopping.domain.event;

import fr.trollgun.optimiam.common.event.DomainEvent;
import fr.trollgun.optimiam.shopping.domain.ShoppingList;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class PurchasesValidatedEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
    private final ShoppingList shoppingList;
    private final int itemsAddedToStock;

    public PurchasesValidatedEvent(ShoppingList shoppingList, int itemsAddedToStock) {
        this.shoppingList = shoppingList;
        this.itemsAddedToStock = itemsAddedToStock;
    }

    @Override
    public String getEventType() {
        return "PurchasesValidated";
    }
}
