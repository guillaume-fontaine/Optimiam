package fr.trollgun.optimiam.recipe.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRecipeStepRequest {

    @Positive(message = "Le numéro d'étape doit être supérieur à zéro")
    private int stepNumber;

    @NotBlank(message = "L'instruction de l'étape est obligatoire")
    private String instruction;

    private Integer durationMinutes;
}
