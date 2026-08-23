package fr.trollgun.optimiam.hardware.api.dto;

import fr.trollgun.optimiam.product.domain.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimulateWeightRequest {
    private BigDecimal weight;
    private Unit unit;
    @Builder.Default
    private boolean stable = true;
}
