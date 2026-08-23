package fr.trollgun.optimiam.recipe.api.dto;

import fr.trollgun.optimiam.product.domain.Unit;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class CreateRecipeIngredientRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private UUID productId;

    @NotNull(message = "La quantité d'ingrédient est obligatoire")
    @Positive(message = "La quantité doit être strictement positive")
    private BigDecimal quantity;

    @NotNull(message = "L'unité de mesure est obligatoire")
    private Unit unit;

    @Builder.Default
    private boolean optional = false;
}
