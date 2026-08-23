package fr.trollgun.optimiam.stock.application;

import fr.trollgun.optimiam.common.event.DomainEvent;
import fr.trollgun.optimiam.common.event.DomainEventPublisher;
import fr.trollgun.optimiam.common.exception.ConflictException;
import fr.trollgun.optimiam.product.application.ProductService;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.stock.api.dto.CreateStockEntryRequest;
import fr.trollgun.optimiam.stock.api.dto.StockExitRequest;
import fr.trollgun.optimiam.stock.api.dto.StockItemResponse;
import fr.trollgun.optimiam.stock.api.dto.StockLossRequest;
import fr.trollgun.optimiam.stock.domain.Location;
import fr.trollgun.optimiam.stock.domain.StockItem;
import fr.trollgun.optimiam.stock.domain.StockItemRepository;
import fr.trollgun.optimiam.stock.domain.StockStatus;
import fr.trollgun.optimiam.transaction.application.StockTransactionService;
import fr.trollgun.optimiam.transaction.domain.LossReason;
import fr.trollgun.optimiam.transaction.domain.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockItemRepository stockItemRepository;

    @Mock
    private ProductService productService;

    @Mock
    private StockTransactionService stockTransactionService;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @InjectMocks
    private StockService stockService;

    private Product product;
    private StockItem stockItem;
    private UUID productId;
    private UUID stockItemId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = Product.builder()
                .id(productId)
                .name("Tomate")
                .defaultUnit(Unit.KG)
                .averageShelfLifeDays(6)
                .build();

        stockItemId = UUID.randomUUID();
        stockItem = StockItem.builder()
                .id(stockItemId)
                .product(product)
                .quantity(new BigDecimal("1.500"))
                .unit(Unit.KG)
                .entryDate(LocalDate.now())
                .expirationDate(LocalDate.now().plusDays(5))
                .location(Location.FRIDGE)
                .status(StockStatus.AVAILABLE)
                .build();
    }

    @Test
    @DisplayName("Doit créer une entrée de stock et publier l'événement")
    void shouldCreateStockEntry() {
        CreateStockEntryRequest request = CreateStockEntryRequest.builder()
                .productId(productId)
                .quantity(new BigDecimal("2.000"))
                .location(Location.FRIDGE)
                .build();

        when(productService.getProductEntity(productId)).thenReturn(product);
        when(stockItemRepository.save(any(StockItem.class))).thenAnswer(invocation -> {
            StockItem s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        StockItemResponse response = stockService.createStockEntry(request);

        assertThat(response).isNotNull();
        assertThat(response.getQuantity()).isEqualByComparingTo("2.000");
        assertThat(response.getUnit()).isEqualTo(Unit.KG);
        verify(stockTransactionService).recordTransaction(
                any(), eq(product), eq(TransactionType.ENTRY), eq(new BigDecimal("2.000")), eq(Unit.KG), isNull(), anyString(), isNull()
        );
        verify(domainEventPublisher).publish(any(DomainEvent.class));
    }

    @Test
    @DisplayName("Doit déduire la quantité lors d'une consommation")
    void shouldExitStock() {
        StockExitRequest request = StockExitRequest.builder()
                .quantity(new BigDecimal("0.500"))
                .reason("Cuisiné pour salade")
                .build();

        when(stockItemRepository.findById(stockItemId)).thenReturn(Optional.of(stockItem));
        when(stockItemRepository.save(any(StockItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockItemResponse response = stockService.exitStock(stockItemId, request);

        assertThat(response.getQuantity()).isEqualByComparingTo("1.000");
        verify(stockTransactionService).recordTransaction(
                eq(stockItemId), eq(product), eq(TransactionType.CONSUMPTION), eq(new BigDecimal("0.500")), eq(Unit.KG), isNull(), eq("Cuisiné pour salade"), isNull()
        );
        verify(domainEventPublisher).publish(any(DomainEvent.class));
    }

    @Test
    @DisplayName("Doit lever une exception si la quantité demandée dépasse le stock")
    void shouldThrowConflictWhenInsufficientStock() {
        StockExitRequest request = StockExitRequest.builder()
                .quantity(new BigDecimal("5.000"))
                .build();

        when(stockItemRepository.findById(stockItemId)).thenReturn(Optional.of(stockItem));

        assertThatThrownBy(() -> stockService.exitStock(stockItemId, request))
                .isInstanceOf(ConflictException.class);

        verify(stockItemRepository, never()).save(any());
    }

    @Test
    @DisplayName("Doit enregistrer une perte de stock avec motif")
    void shouldRecordStockLoss() {
        StockLossRequest request = StockLossRequest.builder()
                .quantity(new BigDecimal("1.500"))
                .lossReason(LossReason.EXPIRED)
                .comment("Oublié au fond du frigo")
                .build();

        when(stockItemRepository.findById(stockItemId)).thenReturn(Optional.of(stockItem));
        when(stockItemRepository.save(any(StockItem.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockItemResponse response = stockService.recordStockLoss(stockItemId, request);

        assertThat(response.getQuantity()).isEqualByComparingTo("0.000");
        assertThat(stockItem.getStatus()).isEqualTo(StockStatus.DISCARDED);
        verify(stockTransactionService).recordTransaction(
                eq(stockItemId), eq(product), eq(TransactionType.LOSS), eq(new BigDecimal("1.500")), eq(Unit.KG), eq(LossReason.EXPIRED), eq("Oublié au fond du frigo"), isNull()
        );
    }
}
