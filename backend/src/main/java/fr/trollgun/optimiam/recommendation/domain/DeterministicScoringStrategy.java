package fr.trollgun.optimiam.recommendation.domain;

import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.api.dto.RecipeResponse;
import fr.trollgun.optimiam.recipe.domain.Recipe;
import fr.trollgun.optimiam.recipe.domain.RecipeIngredient;
import fr.trollgun.optimiam.recommendation.api.dto.MissingIngredientDto;
import fr.trollgun.optimiam.recommendation.api.dto.RecommendationResponse;
import fr.trollgun.optimiam.stock.domain.StockItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DeterministicScoringStrategy implements RecommendationStrategy {

    @Override
    public List<RecommendationResponse> computeRecommendations(RecommendationContext context) {
        LocalDate today = context.getToday() != null ? context.getToday() : LocalDate.now();

        // Indexation du stock disponible par produit
        Map<UUID, List<StockItem>> stockByProduct = context.getAvailableStock().stream()
                .filter(item -> item.getQuantity() != null && item.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.groupingBy(item -> item.getProduct().getId()));

        List<RecommendationResponse> results = new ArrayList<>();

        for (Recipe recipe : context.getRecipes()) {
            // Filtrage par critères optionnels
            if (context.getMaxTimeMinutes() != null && recipe.getTotalTimeMinutes() > context.getMaxTimeMinutes()) {
                continue;
            }
            if (context.getDifficulty() != null && recipe.getDifficulty() != context.getDifficulty()) {
                continue;
            }
            if (context.getTag() != null && !context.getTag().isBlank() && !recipe.getTags().contains(context.getTag())) {
                continue;
            }

            int targetServings = (context.getServings() != null && context.getServings() > 0)
                    ? context.getServings()
                    : (recipe.getServings() != null ? recipe.getServings() : 4);

            double servingsRatio = (recipe.getServings() != null && recipe.getServings() > 0)
                    ? (double) targetServings / recipe.getServings()
                    : 1.0;

            List<RecipeIngredient> requiredIngredients = recipe.getIngredients().stream()
                    .filter(ing -> !ing.isOptional())
                    .collect(Collectors.toList());

            int totalRequiredCount = requiredIngredients.size();
            if (totalRequiredCount == 0) totalRequiredCount = 1;

            int availableCount = 0;
            int urgencyScore = 0;
            List<MissingIngredientDto> missingList = new ArrayList<>();
            List<String> expiringIngredientsUsed = new ArrayList<>();

            for (RecipeIngredient ing : requiredIngredients) {
                Product product = ing.getProduct();
                BigDecimal neededQty = ing.getQuantity().multiply(BigDecimal.valueOf(servingsRatio));
                Unit neededUnit = ing.getUnit();

                List<StockItem> items = stockByProduct.getOrDefault(product.getId(), Collections.emptyList());

                BigDecimal totalAvailableNormalized = BigDecimal.ZERO;
                boolean isUrgent = false;
                long shortestDays = Long.MAX_VALUE;

                for (StockItem item : items) {
                    BigDecimal normalized = convertQuantity(item.getQuantity(), item.getUnit(), neededUnit);
                    totalAvailableNormalized = totalAvailableNormalized.add(normalized);

                    if (item.getExpirationDate() != null) {
                        long days = ChronoUnit.DAYS.between(today, item.getExpirationDate());
                        if (days <= 3) {
                            isUrgent = true;
                            if (days < shortestDays) {
                                shortestDays = days;
                            }
                        }
                    }
                }

                if (isUrgent) {
                    String urgencyDesc;
                    if (shortestDays <= 0) {
                        urgencyDesc = product.getName() + " (expire aujourd'hui)";
                        urgencyScore += 50;
                    } else if (shortestDays == 1) {
                        urgencyDesc = product.getName() + " (expire demain)";
                        urgencyScore += 40;
                    } else {
                        urgencyDesc = product.getName() + " (expire dans " + shortestDays + "j)";
                        urgencyScore += 25;
                    }
                    expiringIngredientsUsed.add(urgencyDesc);
                }

                if (totalAvailableNormalized.compareTo(neededQty) >= 0) {
                    availableCount++;
                } else {
                    BigDecimal missing = neededQty.subtract(totalAvailableNormalized);
                    missingList.add(MissingIngredientDto.builder()
                            .product(ProductResponse.from(product))
                            .requiredQuantity(neededQty.setScale(2, RoundingMode.HALF_UP))
                            .availableQuantity(totalAvailableNormalized.setScale(2, RoundingMode.HALF_UP))
                            .missingQuantity(missing.setScale(2, RoundingMode.HALF_UP))
                            .unitSymbol(neededUnit.getSymbol())
                            .build());
                }
            }

            int matchPercentage = (int) Math.round(((double) availableCount / totalRequiredCount) * 100.0);

            if (context.isOnlyFullStock() && matchPercentage < 100) {
                continue;
            }

            // Calcul du score global déterministe
            double score = (matchPercentage * 0.40) + urgencyScore;

            if (matchPercentage == 100) {
                score += 15; // Bonus stock 100% complet
            }
            if (recipe.getTotalTimeMinutes() <= 20) {
                score += 10; // Bonus recette rapide
            }
            if (recipe.getTags().contains("Anti-gaspi")) {
                score += 10; // Bonus tag anti-gaspi
            }

            // Génération des explications lisibles
            List<String> reasons = new ArrayList<>();
            if (!expiringIngredientsUsed.isEmpty()) {
                reasons.add("🔴 Sauve " + expiringIngredientsUsed.size() + " produit(s) à consommer rapidement : " + String.join(", ", expiringIngredientsUsed));
            }
            if (matchPercentage == 100) {
                reasons.add("✅ 100% des ingrédients indispensables sont dans votre cuisine");
            } else if (matchPercentage >= 70) {
                reasons.add("👍 " + matchPercentage + "% des ingrédients disponibles");
            }
            if (recipe.getTotalTimeMinutes() <= 20) {
                reasons.add("⚡ Prêt rapidement en " + recipe.getTotalTimeMinutes() + " minutes");
            }
            if (recipe.getTags().contains("Anti-gaspi")) {
                reasons.add("🌱 Recette labellisée Anti-Gaspi");
            }
            if (!missingList.isEmpty()) {
                reasons.add("⚠️ " + missingList.size() + " ingrédient(s) à prévoir ou compléter");
            }

            results.add(RecommendationResponse.builder()
                    .recipe(RecipeResponse.from(recipe))
                    .score(Math.round(score * 10.0) / 10.0)
                    .matchPercentage(matchPercentage)
                    .urgencyScore(urgencyScore)
                    .reasons(reasons)
                    .totalIngredientsCount(totalRequiredCount)
                    .availableIngredientsCount(availableCount)
                    .missingIngredientsCount(missingList.size())
                    .missingIngredients(missingList)
                    .expiringIngredientsUsed(expiringIngredientsUsed)
                    .fullyAvailableInStock(matchPercentage == 100)
                    .build());
        }

        // Tri : Score DESC -> Match% DESC -> Temps ASC -> Nom ASC
        results.sort((a, b) -> {
            int cmp = Double.compare(b.getScore(), a.getScore());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(b.getMatchPercentage(), a.getMatchPercentage());
            if (cmp != 0) return cmp;
            cmp = Integer.compare(a.getRecipe().getTotalTimeMinutes(), b.getRecipe().getTotalTimeMinutes());
            if (cmp != 0) return cmp;
            return a.getRecipe().getName().compareTo(b.getRecipe().getName());
        });

        return results;
    }

    private BigDecimal convertQuantity(BigDecimal quantity, Unit fromUnit, Unit toUnit) {
        if (quantity == null || fromUnit == null || toUnit == null) return BigDecimal.ZERO;
        if (fromUnit == toUnit) return quantity;

        // KG <-> G
        if (fromUnit == Unit.KG && toUnit == Unit.G) return quantity.multiply(BigDecimal.valueOf(1000));
        if (fromUnit == Unit.G && toUnit == Unit.KG) return quantity.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

        // L <-> ML
        if (fromUnit == Unit.L && toUnit == Unit.ML) return quantity.multiply(BigDecimal.valueOf(1000));
        if (fromUnit == Unit.ML && toUnit == Unit.L) return quantity.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);

        return quantity;
    }
}
