package fr.trollgun.optimiam.transaction.api;

import fr.trollgun.optimiam.common.dto.PageResponse;
import fr.trollgun.optimiam.transaction.api.dto.LossStatisticsResponse;
import fr.trollgun.optimiam.transaction.api.dto.StockTransactionResponse;
import fr.trollgun.optimiam.transaction.application.StockTransactionService;
import fr.trollgun.optimiam.transaction.domain.TransactionType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Historique des mouvements de stock et statistiques de pertes")
public class StockTransactionController {

    private final StockTransactionService transactionService;

    @GetMapping
    @Operation(summary = "Lister l'historique des transactions de stock")
    public ResponseEntity<PageResponse<StockTransactionResponse>> getTransactions(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(transactionService.getTransactions(type, pageable));
    }

    @GetMapping("/losses/stats")
    @Operation(summary = "Obtenir les statistiques agrégées des pertes et du gaspillage alimentaire")
    public ResponseEntity<LossStatisticsResponse> getLossStatistics(
            @RequestParam(defaultValue = "30") int days
    ) {
        return ResponseEntity.ok(transactionService.getLossStatistics(days));
    }
}
