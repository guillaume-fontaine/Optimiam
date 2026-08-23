package fr.trollgun.optimiam.recommendation.domain;

import fr.trollgun.optimiam.recipe.domain.Difficulty;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.stock.domain.StockItem;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class RecommendationContext {
    private List<StockItem> availableStock;
    private List<Recipe> recipes;
    private Integer servings;
    private Integer maxTimeMinutes;
    private Difficulty difficulty;
    private String tag;
    private boolean onlyFullStock;
    private LocalDate today;
}
