package fr.trollgun.optimiam.hardware.domain;

import fr.trollgun.optimiam.product.domain.Unit;

import java.math.BigDecimal;

public interface ScaleDriver {
    ScaleMeasurement measure();
    void tare();
    void setSimulatedWeight(BigDecimal weight, Unit unit, boolean stable);
}
