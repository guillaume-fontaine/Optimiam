package fr.trollgun.optimiam.planning.api.dto;

import fr.trollgun.optimiam.planning.domain.MealType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMealPlanRequest {

    @NotNull(message = "La date du repas est obligatoire")
    private LocalDate date;

    @NotNull(message = "Le créneau du repas est obligatoire")
    private MealType mealType;

    @NotNull(message = "L'identifiant de la recette est obligatoire")
    private UUID recipeId;

    @Min(value = 1, message = "Le nombre de portions doit être d'au moins 1")
    @Builder.Default
    private Integer servings = 4;

    @Size(max = 500, message = "Les notes ne peuvent pas dépasser 500 caractères")
    private String notes;
}
