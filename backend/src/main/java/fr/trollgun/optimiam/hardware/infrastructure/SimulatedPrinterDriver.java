package fr.trollgun.optimiam.hardware.infrastructure;

import fr.trollgun.optimiam.hardware.domain.PrintJob;
import fr.trollgun.optimiam.hardware.domain.PrinterDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class SimulatedPrinterDriver implements PrinterDriver {

    private final List<PrintJob> history = new CopyOnWriteArrayList<>();

    @Override
    public PrintJob printLabel(PrintJob request) {
        UUID jobId = UUID.randomUUID();

        String labelLayout = String.format(
                "+------------------------------------------+\n" +
                "|         🌱 OPTIMIAM - ÉTIQUETTE STOCK    |\n" +
                "+------------------------------------------+\n" +
                "| Produit    : %-27s |\n" +
                "| Quantité   : %-27s |\n" +
                "| Emplacement: %-27s |\n" +
                "| Entrée le  : %-27s |\n" +
                "| DLC / DDM  : %-27s |\n" +
                "+------------------------------------------+\n" +
                "| Code-barre : |||| %-22s ||| |\n" +
                "+------------------------------------------+",
                request.getProductName() != null ? request.getProductName() : "Produit Inconnu",
                request.getQuantityWithUnit() != null ? request.getQuantityWithUnit() : "-",
                request.getLocation() != null ? request.getLocation() : "FRIDGE",
                request.getEntryDate() != null ? request.getEntryDate().toString() : "-",
                request.getExpirationDate() != null ? request.getExpirationDate().toString() : "À consommer",
                request.getBarcode() != null ? request.getBarcode() : "0000000000000"
        );

        PrintJob printed = PrintJob.builder()
                .jobId(jobId)
                .productName(request.getProductName())
                .barcode(request.getBarcode())
                .entryDate(request.getEntryDate())
                .expirationDate(request.getExpirationDate())
                .quantityWithUnit(request.getQuantityWithUnit())
                .location(request.getLocation())
                .labelContent(labelLayout)
                .printedAt(Instant.now())
                .success(true)
                .build();

        history.add(0, printed);
        log.info("Imprimante thermique simulée : étiquette imprimée pour '{}' [jobId={}]", request.getProductName(), jobId);

        return printed;
    }

    @Override
    public List<PrintJob> getHistory() {
        return Collections.unmodifiableList(history);
    }
}
