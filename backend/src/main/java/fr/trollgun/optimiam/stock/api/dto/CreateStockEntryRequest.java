package fr.trollgun.optimiam.stock.api.dto;

import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.stock.domain.Location;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateStockEntryRequest {

    @NotNull(message = "L'identifiant du produit est obligatoire")
    private UUID productId;

    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être strictement positive")
    private BigDecimal quantity;

    private Unit unit;

    private LocalDate entryDate;

    private LocalDate expirationDate;

    private Location location;

    private String deviceId;
}
