package fr.trollgun.optimiam.transaction.application;

import fr.trollgun.optimiam.common.dto.PageResponse;
import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.transaction.api.dto.LossStatisticsResponse;
import fr.trollgun.optimiam.transaction.api.dto.StockTransactionResponse;
import fr.trollgun.optimiam.transaction.domain.LossReason;
import fr.trollgun.optimiam.transaction.domain.StockTransaction;
import fr.trollgun.optimiam.transaction.domain.StockTransactionRepository;
import fr.trollgun.optimiam.transaction.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockTransactionService {

    private final StockTransactionRepository transactionRepository;

    @Transactional
    public StockTransaction recordTransaction(
            UUID stockItemId,
            Product product,
            TransactionType type,
            BigDecimal quantity,
            Unit unit,
            LossReason lossReason,
            String reason,
            String deviceId
    ) {
        StockTransaction transaction = StockTransaction.builder()
                .stockItemId(stockItemId)
                .product(product)
                .type(type)
                .quantity(quantity)
                .unit(unit)
                .lossReason(lossReason)
                .reason(reason)
                .deviceId(deviceId)
                .build();

        StockTransaction saved = transactionRepository.save(transaction);
        log.info("Transaction enregistrée: {} de {} {} de {} [id={}]",
                type, quantity, unit, product.getName(), saved.getId());
        return saved;
    }

    @Transactional(readOnly = true)
    public PageResponse<StockTransactionResponse> getTransactions(TransactionType type, Pageable pageable) {
        return PageResponse.from(
                transactionRepository.searchTransactions(type, pageable).map(StockTransactionResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public LossStatisticsResponse getLossStatistics(int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        List<StockTransaction> lossTransactions = transactionRepository.findLossesSince(since);

        BigDecimal totalLossWeightKg = BigDecimal.ZERO;
        Map<LossReason, BigDecimal> lossesByReason = new EnumMap<>(LossReason.class);
        Map<Product, List<StockTransaction>> lossesByProduct = new HashMap<>();

        for (StockTransaction t : lossTransactions) {
            BigDecimal qtyKg = convertToKg(t.getQuantity(), t.getUnit());
            totalLossWeightKg = totalLossWeightKg.add(qtyKg);

            if (t.getLossReason() != null) {
                lossesByReason.merge(t.getLossReason(), qtyKg, BigDecimal::add);
            }

            lossesByProduct.computeIfAbsent(t.getProduct(), k -> new ArrayList<>()).add(t);
        }

        List<LossStatisticsResponse.ProductLossItem> topProducts = lossesByProduct.entrySet().stream()
                .map(entry -> {
                    Product p = entry.getKey();
                    List<StockTransaction> txs = entry.getValue();
                    BigDecimal totalQty = txs.stream().map(StockTransaction::getQuantity).reduce(BigDecimal.ZERO, BigDecimal::add);
                    return LossStatisticsResponse.ProductLossItem.builder()
                            .product(ProductResponse.from(p))
                            .totalQuantity(totalQty)
                            .unitSymbol(txs.get(0).getUnit().getSymbol())
                            .occurrences(txs.size())
                            .build();
                })
                .sorted((a, b) -> Long.compare(b.getOccurrences(), a.getOccurrences()))
                .limit(5)
                .collect(Collectors.toList());

        return LossStatisticsResponse.builder()
                .totalLossWeightKg(totalLossWeightKg.setScale(2, RoundingMode.HALF_UP))
                .totalLossOperations(lossTransactions.size())
                .lossesByReason(lossesByReason)
                .topLostProducts(topProducts)
                .build();
    }

    private BigDecimal convertToKg(BigDecimal quantity, Unit unit) {
        if (quantity == null || unit == null) return BigDecimal.ZERO;
        return switch (unit) {
            case KG, L -> quantity;
            case G, ML -> quantity.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
            case PIECE -> quantity.multiply(BigDecimal.valueOf(0.15)); // Estimation moyenne 150g par pièce
        };
    }
}
