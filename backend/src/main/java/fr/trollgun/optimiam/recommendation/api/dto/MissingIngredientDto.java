package fr.trollgun.optimiam.recommendation.api.dto;

import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissingIngredientDto {
    private ProductResponse product;
    private BigDecimal requiredQuantity;
    private BigDecimal availableQuantity;
    private BigDecimal missingQuantity;
    private String unitSymbol;
}
