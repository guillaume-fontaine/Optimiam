package fr.trollgun.optimiam.recommendation.api.dto;

import fr.trollgun.optimiam.recipe.domain.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationRequest {
    private Integer servings;
    private Integer maxTimeMinutes;
    private Difficulty difficulty;
    private String tag;

    @Builder.Default
    private boolean onlyFullStock = false;
}
