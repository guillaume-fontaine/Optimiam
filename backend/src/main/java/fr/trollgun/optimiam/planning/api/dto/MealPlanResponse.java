package fr.trollgun.optimiam.planning.api.dto;

import fr.trollgun.optimiam.planning.domain.MealPlan;
import fr.trollgun.optimiam.planning.domain.MealPlanStatus;
import fr.trollgun.optimiam.planning.domain.MealType;
import fr.trollgun.optimiam.recipe.api.dto.RecipeResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class MealPlanResponse {
    private UUID id;
    private LocalDate date;
    private MealType mealType;
    private String mealTypeLabel;
    private String mealTypeIcon;
    private RecipeResponse recipe;
    private Integer servings;
    private MealPlanStatus status;
    private String statusLabel;
    private String notes;
    private Instant createdAt;
    private Instant updatedAt;

    public static MealPlanResponse from(MealPlan plan) {
        if (plan == null) return null;
        return MealPlanResponse.builder()
                .id(plan.getId())
                .date(plan.getDate())
                .mealType(plan.getMealType())
                .mealTypeLabel(plan.getMealType() != null ? plan.getMealType().getLabel() : null)
                .mealTypeIcon(plan.getMealType() != null ? plan.getMealType().getIcon() : null)
                .recipe(RecipeResponse.from(plan.getRecipe()))
                .servings(plan.getServings())
                .status(plan.getStatus())
                .statusLabel(plan.getStatus() != null ? plan.getStatus().getLabel() : null)
                .notes(plan.getNotes())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}
