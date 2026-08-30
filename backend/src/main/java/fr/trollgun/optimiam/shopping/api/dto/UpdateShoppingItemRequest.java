package fr.trollgun.optimiam.shopping.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateShoppingItemRequest {
    private BigDecimal quantity;
    private BigDecimal purchasedQuantity;
    private Boolean checked;
}
