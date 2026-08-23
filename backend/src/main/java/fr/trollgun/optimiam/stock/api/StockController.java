package fr.trollgun.optimiam.stock.api;

import fr.trollgun.optimiam.common.dto.PageResponse;
import fr.trollgun.optimiam.stock.api.dto.*;
import fr.trollgun.optimiam.stock.application.StockService;
import fr.trollgun.optimiam.stock.domain.Location;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock")
@RequiredArgsConstructor
@Tag(name = "Stock", description = "Gestion des stocks, entrées, sorties, péremptions et pertes")
public class StockController {

    private final StockService stockService;

    @GetMapping
    @Operation(summary = "Rechercher et lister les produits en stock")
    public ResponseEntity<PageResponse<StockItemResponse>> getStock(
            @RequestParam(required = false) Location location,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expirationDate") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(stockService.getStockItems(location, query, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un élément de stock")
    public ResponseEntity<StockItemResponse> getStockItemById(@PathVariable UUID id) {
        return ResponseEntity.ok(stockService.getStockItemById(id));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Lister les produits proches de la péremption ou périmés")
    public ResponseEntity<List<StockItemResponse>> getExpiringStock(@RequestParam(defaultValue = "3") int daysAhead) {
        return ResponseEntity.ok(stockService.getExpiringStockItems(daysAhead));
    }

    @GetMapping("/summary")
    @Operation(summary = "Obtenir les compteurs de stock et statistiques du dashboard")
    public ResponseEntity<StockSummaryResponse> getStockSummary() {
        return ResponseEntity.ok(stockService.getStockSummary());
    }

    @PostMapping("/entries")
    @Operation(summary = "Ajouter un produit en stock (entrée de stock)")
    public ResponseEntity<StockItemResponse> createStockEntry(@Valid @RequestBody CreateStockEntryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.createStockEntry(request));
    }

    @PostMapping("/{id}/exits")
    @Operation(summary = "Consommer ou sortir une quantité de produit du stock")
    public ResponseEntity<StockItemResponse> exitStock(
            @PathVariable UUID id,
            @Valid @RequestBody StockExitRequest request
    ) {
        return ResponseEntity.ok(stockService.exitStock(id, request));
    }

    @PostMapping("/{id}/losses")
    @Operation(summary = "Déclarer une perte / un gaspillage de stock avec motif")
    public ResponseEntity<StockItemResponse> recordLoss(
            @PathVariable UUID id,
            @Valid @RequestBody StockLossRequest request
    ) {
        return ResponseEntity.ok(stockService.recordStockLoss(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un élément de stock")
    public ResponseEntity<Void> deleteStockItem(@PathVariable UUID id) {
        stockService.deleteStockItem(id);
        return ResponseEntity.noContent().build();
    }
}
