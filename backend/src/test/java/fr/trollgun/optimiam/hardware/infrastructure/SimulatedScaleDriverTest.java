package fr.trollgun.optimiam.hardware.infrastructure;

import fr.trollgun.optimiam.hardware.domain.ScaleMeasurement;
import fr.trollgun.optimiam.product.domain.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedScaleDriverTest {

    private SimulatedScaleDriver scaleDriver;

    @BeforeEach
    void setUp() {
        scaleDriver = new SimulatedScaleDriver();
    }

    @Test
    @DisplayName("Doit mesurer le poids par défaut")
    void shouldMeasureDefaultWeight() {
        ScaleMeasurement m = scaleDriver.measure();
        assertThat(m).isNotNull();
        assertThat(m.getWeight()).isEqualByComparingTo(new BigDecimal("0.750"));
        assertThat(m.getUnit()).isEqualTo(Unit.KG);
        assertThat(m.isStable()).isTrue();
    }

    @Test
    @DisplayName("Doit tarer la balance à 0.000")
    void shouldTareScale() {
        scaleDriver.tare();
        ScaleMeasurement m = scaleDriver.measure();
        assertThat(m.getWeight()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Doit injecter une valeur de poids simulée")
    void shouldSetSimulatedWeight() {
        scaleDriver.setSimulatedWeight(new BigDecimal("1.420"), Unit.KG, true);
        ScaleMeasurement m = scaleDriver.measure();
        assertThat(m.getWeight()).isEqualByComparingTo(new BigDecimal("1.420"));
        assertThat(m.isStable()).isTrue();
    }
}
