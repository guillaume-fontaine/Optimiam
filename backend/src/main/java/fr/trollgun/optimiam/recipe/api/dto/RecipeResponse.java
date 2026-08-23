package fr.trollgun.optimiam.recipe.api.dto;

import fr.trollgun.optimiam.recipe.domain.Difficulty;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class RecipeResponse {
    private UUID id;
    private String name;
    private String description;
    private Integer preparationTimeMinutes;
    private Integer cookingTimeMinutes;
    private Integer totalTimeMinutes;
    private Difficulty difficulty;
    private String difficultyLabel;
    private Integer servings;
    private String imageUrl;
    private NutritionDto nutrition;
    private List<RecipeIngredientResponse> ingredients;
    private List<RecipeStepResponse> steps;
    private Set<String> tags;
    private Instant createdAt;
    private Instant updatedAt;

    public static RecipeResponse from(Recipe recipe) {
        if (recipe == null) return null;
        return RecipeResponse.builder()
                .id(recipe.getId())
                .name(recipe.getName())
                .description(recipe.getDescription())
                .preparationTimeMinutes(recipe.getPreparationTimeMinutes())
                .cookingTimeMinutes(recipe.getCookingTimeMinutes())
                .totalTimeMinutes(recipe.getTotalTimeMinutes())
                .difficulty(recipe.getDifficulty())
                .difficultyLabel(recipe.getDifficulty() != null ? recipe.getDifficulty().getLabel() : null)
                .servings(recipe.getServings())
                .imageUrl(recipe.getImageUrl())
                .nutrition(NutritionDto.from(recipe.getNutrition()))
                .ingredients(recipe.getIngredients() != null ? recipe.getIngredients().stream().map(RecipeIngredientResponse::from).collect(Collectors.toList()) : List.of())
                .steps(recipe.getSteps() != null ? recipe.getSteps().stream().map(RecipeStepResponse::from).collect(Collectors.toList()) : List.of())
                .tags(recipe.getTags())
                .createdAt(recipe.getCreatedAt())
                .updatedAt(recipe.getUpdatedAt())
                .build();
    }
}
