package fr.trollgun.optimiam.planning.domain.event;

import fr.trollgun.optimiam.common.event.DomainEvent;
import fr.trollgun.optimiam.planning.domain.MealPlan;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class MealPlanCreatedEvent implements DomainEvent {
    private final UUID eventId = UUID.randomUUID();
    private final Instant occurredAt = Instant.now();
    private final MealPlan mealPlan;

    public MealPlanCreatedEvent(MealPlan mealPlan) {
        this.mealPlan = mealPlan;
    }

    @Override
    public String getEventType() {
        return "MealPlanCreated";
    }
}
