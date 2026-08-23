package fr.trollgun.optimiam.stock.application;

import fr.trollgun.optimiam.common.dto.PageResponse;
import fr.trollgun.optimiam.common.event.DomainEventPublisher;
import fr.trollgun.optimiam.common.exception.ConflictException;
import fr.trollgun.optimiam.common.exception.ErrorCode;
import fr.trollgun.optimiam.common.exception.ResourceNotFoundException;
import fr.trollgun.optimiam.product.application.ProductService;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.stock.api.dto.*;
import fr.trollgun.optimiam.stock.domain.Location;
import fr.trollgun.optimiam.stock.domain.StockItem;
import fr.trollgun.optimiam.stock.domain.StockItemRepository;
import fr.trollgun.optimiam.stock.domain.StockStatus;
import fr.trollgun.optimiam.stock.domain.event.StockEntryCreatedEvent;
import fr.trollgun.optimiam.stock.domain.event.StockExitCreatedEvent;
import fr.trollgun.optimiam.stock.domain.event.StockLossRecordedEvent;
import fr.trollgun.optimiam.transaction.api.dto.LossStatisticsResponse;
import fr.trollgun.optimiam.transaction.application.StockTransactionService;
import fr.trollgun.optimiam.transaction.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockService {

    private final StockItemRepository stockItemRepository;
    private final ProductService productService;
    private final StockTransactionService stockTransactionService;
    private final DomainEventPublisher domainEventPublisher;

    @Transactional(readOnly = true)
    public PageResponse<StockItemResponse> getStockItems(Location location, String query, Pageable pageable) {
        LocalDate today = LocalDate.now();
        return PageResponse.from(
                stockItemRepository.searchAvailableStock(location, query, pageable)
                        .map(item -> StockItemResponse.from(item, today))
        );
    }

    @Transactional(readOnly = true)
    public List<StockItem> getAllAvailableStock() {
        return stockItemRepository.findByQuantityGreaterThanOrderByExpirationDateAsc(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public StockItemResponse getStockItemById(UUID id) {
        return StockItemResponse.from(getStockItemEntity(id), LocalDate.now());
    }

    @Transactional(readOnly = true)
    public StockItem getStockItemEntity(UUID id) {
        return stockItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Élément de stock introuvable avec l'identifiant: " + id, ErrorCode.STOCK_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public List<StockItemResponse> getExpiringStockItems(int daysAhead) {
        LocalDate today = LocalDate.now();
        LocalDate maxDate = today.plusDays(daysAhead);

        return stockItemRepository.findByQuantityGreaterThanAndExpirationDateBeforeOrderByExpirationDateAsc(
                        BigDecimal.ZERO, maxDate.plusDays(1))
                .stream()
                .map(item -> StockItemResponse.from(item, today))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StockSummaryResponse getStockSummary() {
        LocalDate today = LocalDate.now();
        long available = stockItemRepository.countAvailableItems();
        long expiringSoon = stockItemRepository.countExpiringItems(today.plusDays(3));
        long expired = stockItemRepository.countExpiringItems(today.minusDays(1));

        LossStatisticsResponse stats = stockTransactionService.getLossStatistics(30);

        return StockSummaryResponse.builder()
                .totalAvailableItems(available)
                .expiringSoonItems(expiringSoon)
                .expiredItems(expired)
                .totalLossesWeightKg(stats.getTotalLossWeightKg())
                .totalLossesCount(stats.getTotalLossOperations())
                .build();
    }

    @Transactional
    public StockItemResponse createStockEntry(CreateStockEntryRequest request) {
        Product product = productService.getProductEntity(request.getProductId());
        LocalDate today = LocalDate.now();

        LocalDate entryDate = request.getEntryDate() != null ? request.getEntryDate() : today;
        LocalDate expirationDate = request.getExpirationDate();

        if (expirationDate == null && product.getAverageShelfLifeDays() != null) {
            expirationDate = entryDate.plusDays(product.getAverageShelfLifeDays());
        }

        Unit unit = request.getUnit() != null ? request.getUnit() : product.getDefaultUnit();
        Location location = request.getLocation() != null ? request.getLocation() : Location.FRIDGE;

        StockItem stockItem = StockItem.builder()
                .product(product)
                .quantity(request.getQuantity())
                .unit(unit)
                .entryDate(entryDate)
                .expirationDate(expirationDate)
                .location(location)
                .status(StockStatus.AVAILABLE)
                .build();

        StockItem saved = stockItemRepository.save(stockItem);

        // Transaction d'entrée
        stockTransactionService.recordTransaction(
                saved.getId(),
                product,
                TransactionType.ENTRY,
                request.getQuantity(),
                unit,
                null,
                "Entrée initiale en stock",
                request.getDeviceId()
        );

        // Publication d'événement de domaine
        domainEventPublisher.publish(new StockEntryCreatedEvent(saved));

        log.info("Entrée de stock créée: {} {} de {} [id={}]",
                saved.getQuantity(), saved.getUnit(), product.getName(), saved.getId());

        return StockItemResponse.from(saved, today);
    }

    @Transactional
    public StockItemResponse exitStock(UUID stockItemId, StockExitRequest request) {
        StockItem item = getStockItemEntity(stockItemId);

        if (request.getQuantity().compareTo(item.getQuantity()) > 0) {
            throw new ConflictException("Quantité demandée (" + request.getQuantity() + ") supérieure au stock disponible (" + item.getQuantity() + ")", ErrorCode.INSUFFICIENT_STOCK);
        }

        BigDecimal newQuantity = item.getQuantity().subtract(request.getQuantity());
        item.setQuantity(newQuantity);

        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            item.setStatus(StockStatus.CONSUMED);
        }

        StockItem updated = stockItemRepository.save(item);

        // Transaction de sortie
        stockTransactionService.recordTransaction(
                updated.getId(),
                item.getProduct(),
                TransactionType.CONSUMPTION,
                request.getQuantity(),
                item.getUnit(),
                null,
                request.getReason() != null ? request.getReason() : "Consommation manuelle",
                request.getDeviceId()
        );

        // Publication événement
        domainEventPublisher.publish(new StockExitCreatedEvent(
                updated.getId(),
                item.getProduct(),
                request.getQuantity(),
                item.getUnit(),
                request.getReason()
        ));

        log.info("Sortie de stock effectuée: {} {} de {} [reste={}]",
                request.getQuantity(), item.getUnit(), item.getProduct().getName(), newQuantity);

        return StockItemResponse.from(updated, LocalDate.now());
    }

    @Transactional
    public StockItemResponse recordStockLoss(UUID stockItemId, StockLossRequest request) {
        StockItem item = getStockItemEntity(stockItemId);

        if (request.getQuantity().compareTo(item.getQuantity()) > 0) {
            throw new ConflictException("Quantité déclarée en perte (" + request.getQuantity() + ") supérieure au stock disponible (" + item.getQuantity() + ")", ErrorCode.INSUFFICIENT_STOCK);
        }

        BigDecimal newQuantity = item.getQuantity().subtract(request.getQuantity());
        item.setQuantity(newQuantity);

        if (newQuantity.compareTo(BigDecimal.ZERO) == 0) {
            item.setStatus(StockStatus.DISCARDED);
        }

        StockItem updated = stockItemRepository.save(item);

        // Transaction de perte
        stockTransactionService.recordTransaction(
                updated.getId(),
                item.getProduct(),
                TransactionType.LOSS,
                request.getQuantity(),
                item.getUnit(),
                request.getLossReason(),
                request.getComment() != null ? request.getComment() : "Perte déclarée: " + request.getLossReason().getLabel(),
                request.getDeviceId()
        );

        // Publication événement
        domainEventPublisher.publish(new StockLossRecordedEvent(
                updated.getId(),
                item.getProduct(),
                request.getQuantity(),
                item.getUnit(),
                request.getLossReason(),
                request.getComment()
        ));

        log.warn("Perte enregistrée: {} {} de {} (motif: {}) [reste={}]",
                request.getQuantity(), item.getUnit(), item.getProduct().getName(), request.getLossReason(), newQuantity);

        return StockItemResponse.from(updated, LocalDate.now());
    }

    @Transactional
    public void deleteStockItem(UUID id) {
        StockItem item = getStockItemEntity(id);
        stockItemRepository.delete(item);
        log.info("StockItem supprimé de la base: {}", id);
    }
}
