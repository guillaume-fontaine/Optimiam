package fr.trollgun.optimiam.recipe.api.dto;

import fr.trollgun.optimiam.recipe.domain.Difficulty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRecipeRequest {

    @NotBlank(message = "Le nom de la recette est obligatoire")
    @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères")
    private String name;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    private String description;

    @Min(value = 0, message = "Le temps de préparation ne peut pas être négatif")
    @Builder.Default
    private Integer preparationTimeMinutes = 15;

    @Min(value = 0, message = "Le temps de cuisson ne peut pas être négatif")
    @Builder.Default
    private Integer cookingTimeMinutes = 0;

    @NotNull(message = "La difficulté est obligatoire")
    @Builder.Default
    private Difficulty difficulty = Difficulty.EASY;

    @Min(value = 1, message = "Le nombre de portions doit être d'au moins 1")
    @Builder.Default
    private Integer servings = 4;

    @Size(max = 500, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
    private String imageUrl;

    private NutritionDto nutrition;

    @Valid
    @NotEmpty(message = "Une recette doit comporter au moins un ingrédient")
    @Builder.Default
    private List<CreateRecipeIngredientRequest> ingredients = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<CreateRecipeStepRequest> steps = new ArrayList<>();

    @Builder.Default
    private Set<String> tags = new HashSet<>();
}
