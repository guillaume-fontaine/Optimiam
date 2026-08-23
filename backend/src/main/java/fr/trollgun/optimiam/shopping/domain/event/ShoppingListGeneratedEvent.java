package fr.trollgun.optimiam.shopping.domain.event;

import fr.trollgun.optimiam.common.event.DomainEvent;
import fr.trollgun.optimiam.shopping.domain.ShoppingList;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class ShoppingListGeneratedEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
    private final ShoppingList shoppingList;

    public ShoppingListGeneratedEvent(ShoppingList shoppingList) {
        this.shoppingList = shoppingList;
    }

    @Override
    public String getEventType() {
        return "ShoppingListGenerated";
    }
}
