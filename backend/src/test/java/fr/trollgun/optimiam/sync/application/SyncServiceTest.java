package fr.trollgun.optimiam.sync.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.trollgun.optimiam.common.exception.ConflictException;
import fr.trollgun.optimiam.common.exception.ErrorCode;
import fr.trollgun.optimiam.planning.application.MealPlanService;
import fr.trollgun.optimiam.shopping.application.ShoppingListService;
import fr.trollgun.optimiam.stock.api.dto.StockItemResponse;
import fr.trollgun.optimiam.stock.application.StockService;
import fr.trollgun.optimiam.sync.api.dto.SyncBatchRequest;
import fr.trollgun.optimiam.sync.api.dto.SyncBatchResponse;
import fr.trollgun.optimiam.sync.api.dto.SyncOperationDto;
import fr.trollgun.optimiam.sync.domain.SyncOperationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncServiceTest {

    @Mock
    private StockService stockService;

    @Mock
    private MealPlanService mealPlanService;

    @Mock
    private ShoppingListService shoppingListService;

    @Spy
    private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private SyncService syncService;

    private UUID stockItemId;

    @BeforeEach
    void setUp() {
        stockItemId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Doit traiter avec succès un lot d'opérations hors-ligne")
    void shouldProcessBatchSuccessfully() {
        SyncOperationDto op = SyncOperationDto.builder()
                .operationId(UUID.randomUUID())
                .type(SyncOperationType.EXIT_STOCK)
                .entityId(stockItemId)
                .payload(Map.of("quantity", 0.5, "reason", "Cuisine"))
                .build();

        when(stockService.exitStock(eq(stockItemId), any())).thenReturn(StockItemResponse.builder()
                .id(stockItemId)
                .quantity(new BigDecimal("1.5"))
                .build());

        SyncBatchResponse response = syncService.processBatch(SyncBatchRequest.builder()
                .clientId("pwa-client-1")
                .operations(List.of(op))
                .build());

        assertThat(response).isNotNull();
        assertThat(response.getSyncedCount()).isEqualTo(1);
        assertThat(response.getConflictCount()).isEqualTo(0);
        assertThat(response.getResults().get(0).isSuccess()).isTrue();
    }

    @Test
    @DisplayName("Doit capturer un conflit si le stock disponible est insuffisant")
    void shouldHandleStockConflictGracefully() {
        SyncOperationDto op = SyncOperationDto.builder()
                .operationId(UUID.randomUUID())
                .type(SyncOperationType.EXIT_STOCK)
                .entityId(stockItemId)
                .payload(Map.of("quantity", 5.0, "reason", "Cuisine"))
                .build();

        when(stockService.exitStock(eq(stockItemId), any())).thenThrow(new ConflictException("Stock insuffisant", ErrorCode.INSUFFICIENT_STOCK));

        SyncBatchResponse response = syncService.processBatch(SyncBatchRequest.builder()
                .clientId("pwa-client-1")
                .operations(List.of(op))
                .build());

        assertThat(response).isNotNull();
        assertThat(response.getSyncedCount()).isEqualTo(0);
        assertThat(response.getConflictCount()).isEqualTo(1);
        assertThat(response.getResults().get(0).isConflict()).isTrue();
        assertThat(response.getResults().get(0).getMessage()).contains("Stock insuffisant");
    }
}
