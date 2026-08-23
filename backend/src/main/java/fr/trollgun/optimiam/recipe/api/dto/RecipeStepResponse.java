package fr.trollgun.optimiam.recipe.api.dto;

import fr.trollgun.optimiam.recipe.domain.RecipeStep;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class RecipeStepResponse {
    private UUID id;
    private int stepNumber;
    private String instruction;
    private Integer durationMinutes;

    public static RecipeStepResponse from(RecipeStep step) {
        if (step == null) return null;
        return RecipeStepResponse.builder()
                .id(step.getId())
                .stepNumber(step.getStepNumber())
                .instruction(step.getInstruction())
                .durationMinutes(step.getDurationMinutes())
                .build();
    }
}
