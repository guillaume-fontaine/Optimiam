package fr.trollgun.optimiam.hardware.infrastructure;

import fr.trollgun.optimiam.hardware.domain.PrintJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class SimulatedPrinterDriverTest {

    private SimulatedPrinterDriver printerDriver;

    @BeforeEach
    void setUp() {
        printerDriver = new SimulatedPrinterDriver();
    }

    @Test
    @DisplayName("Doit formater et imprimer une étiquette avec succès")
    void shouldPrintLabel() {
        PrintJob request = PrintJob.builder()
                .productName("Courgette")
                .barcode("3228857000166")
                .quantityWithUnit("0.500 kg")
                .location("FRIDGE")
                .entryDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(5))
                .build();

        PrintJob result = printerDriver.printLabel(request);

        assertThat(result).isNotNull();
        assertThat(result.getJobId()).isNotNull();
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getLabelContent()).contains("OPTIMIAM - ÉTIQUETTE STOCK");
        assertThat(result.getLabelContent()).contains("Courgette");

        assertThat(printerDriver.getHistory()).hasSize(1);
    }
}
