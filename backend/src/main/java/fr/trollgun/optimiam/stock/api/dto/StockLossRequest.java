package fr.trollgun.optimiam.stock.api.dto;

import fr.trollgun.optimiam.transaction.domain.LossReason;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockLossRequest {

    @NotNull(message = "La quantité perdue est obligatoire")
    @Positive(message = "La quantité perdue doit être strictement positive")
    private BigDecimal quantity;

    @NotNull(message = "Le motif de la perte est obligatoire")
    private LossReason lossReason;

    private String comment;

    private String deviceId;
}
