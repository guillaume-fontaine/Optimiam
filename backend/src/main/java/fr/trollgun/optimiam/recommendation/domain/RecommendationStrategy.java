package fr.trollgun.optimiam.recommendation.domain;

import fr.trollgun.optimiam.recommendation.api.dto.RecommendationResponse;

import java.util.List;

public interface RecommendationStrategy {
    List<RecommendationResponse> computeRecommendations(RecommendationContext context);
}
