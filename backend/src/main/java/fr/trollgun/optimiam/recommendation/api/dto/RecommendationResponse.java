package fr.trollgun.optimiam.recommendation.api.dto;

import fr.trollgun.optimiam.recipe.api.dto.RecipeResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private RecipeResponse recipe;
    private double score;
    private int matchPercentage;
    private int urgencyScore;
    private List<String> reasons;
    private int totalIngredientsCount;
    private int availableIngredientsCount;
    private int missingIngredientsCount;
    private List<MissingIngredientDto> missingIngredients;
    private List<String> expiringIngredientsUsed;
    private boolean fullyAvailableInStock;
}
