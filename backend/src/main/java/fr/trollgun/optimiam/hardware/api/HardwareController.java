package fr.trollgun.optimiam.hardware.api;

import fr.trollgun.optimiam.hardware.api.dto.PrintLabelDto;
import fr.trollgun.optimiam.hardware.api.dto.ScanBarcodeDto;
import fr.trollgun.optimiam.hardware.api.dto.SimulateWeightRequest;
import fr.trollgun.optimiam.hardware.domain.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/hardware")
@RequiredArgsConstructor
@Tag(name = "Hardware Simulé (HAL)", description = "Abstraction matérielle et simulateurs pour Balance, Imprimante et Scanner")
public class HardwareController {

    private final ScaleDriver scaleDriver;
    private final PrinterDriver printerDriver;
    private final ScannerDriver scannerDriver;

    // --- ⚖️ BALANCE CONNECTÉE ---

    @GetMapping("/scale/measure")
    @Operation(summary = "Lire la mesure de poids courante de la balance")
    public ResponseEntity<ScaleMeasurement> getMeasurement() {
        return ResponseEntity.ok(scaleDriver.measure());
    }

    @PostMapping("/scale/tare")
    @Operation(summary = "Tarer la balance à zéro")
    public ResponseEntity<ScaleMeasurement> tareScale() {
        scaleDriver.tare();
        return ResponseEntity.ok(scaleDriver.measure());
    }

    @PostMapping("/scale/simulate")
    @Operation(summary = "Injecter un poids simulé sur la balance connectée")
    public ResponseEntity<ScaleMeasurement> simulateWeight(@RequestBody SimulateWeightRequest request) {
        scaleDriver.setSimulatedWeight(request.getWeight(), request.getUnit(), request.isStable());
        return ResponseEntity.ok(scaleDriver.measure());
    }

    // --- 🏷️ IMPRIMANTE THERMIQUE D'ÉTIQUETTES ---

    @PostMapping("/printer/print-label")
    @Operation(summary = "Imprimer une étiquette de traçabilité de stock")
    public ResponseEntity<PrintJob> printLabel(@RequestBody PrintLabelDto dto) {
        PrintJob job = PrintJob.builder()
                .productName(dto.getProductName())
                .barcode(dto.getBarcode())
                .entryDate(dto.getEntryDate())
                .expirationDate(dto.getExpirationDate())
                .quantityWithUnit(dto.getQuantityWithUnit())
                .location(dto.getLocation())
                .build();
        return ResponseEntity.ok(printerDriver.printLabel(job));
    }

    @GetMapping("/printer/jobs")
    @Operation(summary = "Consulter l'historique des étiquettes imprimées")
    public ResponseEntity<List<PrintJob>> getPrintJobs() {
        return ResponseEntity.ok(printerDriver.getHistory());
    }

    // --- 📱 SCANNER DE CODES-BARRES ---

    @PostMapping("/scanner/scan")
    @Operation(summary = "Scanner un code-barres et rechercher le produit et son stock")
    public ResponseEntity<ScanResult> scanBarcode(@Valid @RequestBody ScanBarcodeDto dto) {
        return ResponseEntity.ok(scannerDriver.scanBarcode(dto.getBarcode()));
    }
}
