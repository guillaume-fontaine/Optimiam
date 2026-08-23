package fr.trollgun.optimiam.recommendation.application;

import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.recipe.domain.RecipeRepository;
import fr.trollgun.optimiam.recommendation.api.dto.RecommendationRequest;
import fr.trollgun.optimiam.recommendation.api.dto.RecommendationResponse;
import fr.trollgun.optimiam.recommendation.domain.RecommendationContext;
import fr.trollgun.optimiam.recommendation.domain.RecommendationStrategy;
import fr.trollgun.optimiam.stock.domain.StockItem;
import fr.trollgun.optimiam.stock.domain.StockItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final RecipeRepository recipeRepository;
    private final StockItemRepository stockItemRepository;
    private final RecommendationStrategy recommendationStrategy;

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getRecommendations(RecommendationRequest request) {
        List<StockItem> availableStock = stockItemRepository.findByQuantityGreaterThanOrderByExpirationDateAsc(BigDecimal.ZERO);
        List<Recipe> candidateRecipes = recipeRepository.findAllWithIngredients();

        RecommendationContext context = RecommendationContext.builder()
                .availableStock(availableStock)
                .recipes(candidateRecipes)
                .servings(request != null ? request.getServings() : null)
                .maxTimeMinutes(request != null ? request.getMaxTimeMinutes() : null)
                .difficulty(request != null ? request.getDifficulty() : null)
                .tag(request != null ? request.getTag() : null)
                .onlyFullStock(request != null && request.isOnlyFullStock())
                .today(LocalDate.now())
                .build();

        List<RecommendationResponse> recommendations = recommendationStrategy.computeRecommendations(context);
        log.info("Calcul de recommandations : {} recettes classées pour {} items en stock",
                recommendations.size(), availableStock.size());

        return recommendations;
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> getDailyTopRecommendations(int limit) {
        List<RecommendationResponse> all = getRecommendations(RecommendationRequest.builder().build());
        return all.stream().limit(limit).collect(Collectors.toList());
    }
}
