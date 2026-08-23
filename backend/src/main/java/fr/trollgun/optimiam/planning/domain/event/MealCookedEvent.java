package fr.trollgun.optimiam.planning.domain.event;

import fr.trollgun.optimiam.common.event.DomainEvent;
import fr.trollgun.optimiam.planning.domain.MealPlan;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class MealCookedEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
    private final MealPlan mealPlan;
    private final boolean stockDeducted;

    public MealCookedEvent(MealPlan mealPlan, boolean stockDeducted) {
        this.mealPlan = mealPlan;
        this.stockDeducted = stockDeducted;
    }

    @Override
    public String getEventType() {
        return "MealCooked";
    }
}
