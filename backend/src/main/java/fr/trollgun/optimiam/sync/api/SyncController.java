package fr.trollgun.optimiam.sync.api;

import fr.trollgun.optimiam.sync.api.dto.SyncBatchRequest;
import fr.trollgun.optimiam.sync.api.dto.SyncBatchResponse;
import fr.trollgun.optimiam.sync.application.SyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/sync")
@RequiredArgsConstructor
@Tag(name = "Synchronisation Offline", description = "Traitement des opérations différées en mode hors-ligne et réconciliation des données")
public class SyncController {

    private final SyncService syncService;

    @PostMapping("/batch")
    @Operation(summary = "Synchroniser un lot d'opérations exécutées hors-ligne par le client PWA")
    public ResponseEntity<SyncBatchResponse> syncBatch(@Valid @RequestBody SyncBatchRequest request) {
        return ResponseEntity.ok(syncService.processBatch(request));
    }

    @GetMapping("/status")
    @Operation(summary = "Vérifier la connectivité et récupérer l'horodatage serveur")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        return ResponseEntity.ok(Map.of(
                "status", "ONLINE",
                "serverTime", Instant.now(),
                "version", "1.0.0"
        ));
    }
}
