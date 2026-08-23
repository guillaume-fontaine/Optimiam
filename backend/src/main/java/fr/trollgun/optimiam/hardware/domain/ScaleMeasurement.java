package fr.trollgun.optimiam.hardware.domain;

import fr.trollgun.optimiam.product.domain.Unit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScaleMeasurement {
    private BigDecimal weight;
    private Unit unit;
    private boolean stable;
    @Builder.Default
    private Instant timestamp = Instant.now();
}
