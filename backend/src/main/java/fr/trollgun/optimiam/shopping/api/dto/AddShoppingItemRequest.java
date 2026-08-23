package fr.trollgun.optimiam.shopping.api.dto;

import fr.trollgun.optimiam.product.domain.Unit;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddShoppingItemRequest {

    @NotNull(message = "Le produit est obligatoire")
    private UUID productId;

    @NotNull(message = "La quantité est obligatoire")
    @DecimalMin(value = "0.001", message = "La quantité doit être supérieure à 0")
    private BigDecimal quantity;

    private Unit unit;
}
