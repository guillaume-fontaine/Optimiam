package fr.trollgun.optimiam.stock.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockItemRepository extends JpaRepository<StockItem, UUID> {

    List<StockItem> findByQuantityGreaterThanOrderByExpirationDateAsc(BigDecimal minQuantity);

    List<StockItem> findByQuantityGreaterThanAndLocationOrderByExpirationDateAsc(BigDecimal minQuantity, Location location);

    List<StockItem> findByQuantityGreaterThanAndProductIdOrderByExpirationDateAsc(BigDecimal minQuantity, UUID productId);

    List<StockItem> findByQuantityGreaterThanAndExpirationDateBeforeOrderByExpirationDateAsc(BigDecimal minQuantity, LocalDate date);

    List<StockItem> findByQuantityGreaterThanAndExpirationDateBetweenOrderByExpirationDateAsc(BigDecimal minQuantity, LocalDate start, LocalDate end);

    @Query("SELECT s FROM StockItem s WHERE s.quantity > 0 AND (:location IS NULL OR s.location = :location) AND (:query IS NULL OR LOWER(s.product.name) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<StockItem> searchAvailableStock(@Param("location") Location location, @Param("query") String query, Pageable pageable);

    @Query("SELECT COUNT(s) FROM StockItem s WHERE s.quantity > 0")
    long countAvailableItems();

    @Query("SELECT COUNT(s) FROM StockItem s WHERE s.quantity > 0 AND s.expirationDate IS NOT NULL AND s.expirationDate <= :maxDate")
    long countExpiringItems(@Param("maxDate") LocalDate maxDate);
}
