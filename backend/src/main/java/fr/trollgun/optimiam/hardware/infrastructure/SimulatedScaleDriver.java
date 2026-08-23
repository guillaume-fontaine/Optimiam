package fr.trollgun.optimiam.hardware.infrastructure;

import fr.trollgun.optimiam.hardware.domain.ScaleDriver;
import fr.trollgun.optimiam.hardware.domain.ScaleMeasurement;
import fr.trollgun.optimiam.product.domain.Unit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

@Slf4j
@Component
public class SimulatedScaleDriver implements ScaleDriver {

    private BigDecimal currentWeight = new BigDecimal("0.750");
    private Unit currentUnit = Unit.KG;
    private boolean isStable = true;

    @Override
    public synchronized ScaleMeasurement measure() {
        return ScaleMeasurement.builder()
                .weight(currentWeight.setScale(3, RoundingMode.HALF_UP))
                .unit(currentUnit)
                .stable(isStable)
                .timestamp(Instant.now())
                .build();
    }

    @Override
    public synchronized void tare() {
        this.currentWeight = BigDecimal.ZERO;
        this.isStable = true;
        log.info("Balance simulée : tare effectuée (0.000 {})", currentUnit);
    }

    @Override
    public synchronized void setSimulatedWeight(BigDecimal weight, Unit unit, boolean stable) {
        this.currentWeight = weight != null ? weight : BigDecimal.ZERO;
        this.currentUnit = unit != null ? unit : Unit.KG;
        this.isStable = stable;
        log.info("Balance simulée : nouveau poids injecté = {} {} (stable={})", currentWeight, currentUnit, isStable);
    }
}
