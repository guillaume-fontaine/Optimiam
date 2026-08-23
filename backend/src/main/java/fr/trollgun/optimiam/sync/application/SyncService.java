package fr.trollgun.optimiam.sync.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.trollgun.optimiam.planning.application.MealPlanService;
import fr.trollgun.optimiam.shopping.api.dto.UpdateShoppingItemRequest;
import fr.trollgun.optimiam.shopping.application.ShoppingListService;
import fr.trollgun.optimiam.stock.api.dto.CreateStockEntryRequest;
import fr.trollgun.optimiam.stock.api.dto.StockExitRequest;
import fr.trollgun.optimiam.stock.api.dto.StockLossRequest;
import fr.trollgun.optimiam.stock.application.StockService;
import fr.trollgun.optimiam.sync.api.dto.SyncBatchRequest;
import fr.trollgun.optimiam.sync.api.dto.SyncBatchResponse;
import fr.trollgun.optimiam.sync.api.dto.SyncOperationDto;
import fr.trollgun.optimiam.sync.api.dto.SyncOperationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final StockService stockService;
    private final MealPlanService mealPlanService;
    private final ShoppingListService shoppingListService;
    private final ObjectMapper objectMapper;

    @Transactional
    public SyncBatchResponse processBatch(SyncBatchRequest request) {
        List<SyncOperationResult> results = new ArrayList<>();
        int synced = 0;
        int conflicts = 0;

        if (request == null || request.getOperations() == null) {
            return SyncBatchResponse.builder()
                    .totalOperations(0)
                    .syncedCount(0)
                    .conflictCount(0)
                    .results(results)
                    .serverTimestamp(Instant.now())
                    .build();
        }

        log.info("Traitement d'un lot de synchronisation de {} opérations pour le client {}",
                request.getOperations().size(), request.getClientId());

        for (SyncOperationDto op : request.getOperations()) {
            try {
                Object resultEntity = executeOperation(op);
                results.add(SyncOperationResult.builder()
                        .operationId(op.getOperationId())
                        .success(true)
                        .conflict(false)
                        .message("Opération appliquée avec succès")
                        .resultEntity(resultEntity)
                        .build());
                synced++;
            } catch (Exception e) {
                log.warn("Conflit ou erreur lors de la synchronisation de l'opération {} [{}]: {}",
                        op.getOperationId(), op.getType(), e.getMessage());
                results.add(SyncOperationResult.builder()
                        .operationId(op.getOperationId())
                        .success(false)
                        .conflict(true)
                        .message(e.getMessage())
                        .build());
                conflicts++;
            }
        }

        return SyncBatchResponse.builder()
                .totalOperations(request.getOperations().size())
                .syncedCount(synced)
                .conflictCount(conflicts)
                .results(results)
                .serverTimestamp(Instant.now())
                .build();
    }

    private Object executeOperation(SyncOperationDto op) {
        if (op.getType() == null) return null;

        switch (op.getType()) {
            case CREATE_STOCK_ENTRY:
                CreateStockEntryRequest createReq = objectMapper.convertValue(op.getPayload(), CreateStockEntryRequest.class);
                return stockService.createStockEntry(createReq);

            case EXIT_STOCK:
                StockExitRequest exitReq = objectMapper.convertValue(op.getPayload(), StockExitRequest.class);
                return stockService.exitStock(op.getEntityId(), exitReq);

            case RECORD_LOSS:
                StockLossRequest lossReq = objectMapper.convertValue(op.getPayload(), StockLossRequest.class);
                return stockService.recordStockLoss(op.getEntityId(), lossReq);

            case COOK_MEAL:
                return mealPlanService.markAsCooked(op.getEntityId(), true);

            case UPDATE_SHOPPING_ITEM:
                UpdateShoppingItemRequest shopReq = objectMapper.convertValue(op.getPayload(), UpdateShoppingItemRequest.class);
                UUID listId = UUID.fromString((String) op.getPayload().get("listId"));
                return shoppingListService.updateItem(listId, op.getEntityId(), shopReq);

            default:
                log.info("Opération générique synchronisée: {}", op.getType());
                return null;
        }
    }
}
