package fr.trollgun.optimiam.recipe.api.dto;

import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.recipe.domain.RecipeIngredient;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class RecipeIngredientResponse {
    private UUID id;
    private ProductResponse product;
    private BigDecimal quantity;
    private Unit unit;
    private String unitLabel;
    private String unitSymbol;
    private boolean optional;

    public static RecipeIngredientResponse from(RecipeIngredient ingredient) {
        if (ingredient == null) return null;
        return RecipeIngredientResponse.builder()
                .id(ingredient.getId())
                .product(ProductResponse.from(ingredient.getProduct()))
                .quantity(ingredient.getQuantity())
                .unit(ingredient.getUnit())
                .unitLabel(ingredient.getUnit() != null ? ingredient.getUnit().getLabel() : null)
                .unitSymbol(ingredient.getUnit() != null ? ingredient.getUnit().getSymbol() : null)
                .optional(ingredient.isOptional())
                .build();
    }
}
