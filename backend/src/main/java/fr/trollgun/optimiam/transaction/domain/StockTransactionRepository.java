package fr.trollgun.optimiam.transaction.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {

    Page<StockTransaction> findAllByOrderByTimestampDesc(Pageable pageable);

    Page<StockTransaction> findByTypeOrderByTimestampDesc(TransactionType type, Pageable pageable);

    List<StockTransaction> findByTypeAndTimestampBetween(TransactionType type, Instant start, Instant end);

    @Query("SELECT t FROM StockTransaction t WHERE (:type IS NULL OR t.type = :type) ORDER BY t.timestamp DESC")
    Page<StockTransaction> searchTransactions(@Param("type") TransactionType type, Pageable pageable);

    @Query("SELECT t FROM StockTransaction t WHERE t.type = 'LOSS' AND t.timestamp >= :since ORDER BY t.timestamp DESC")
    List<StockTransaction> findLossesSince(@Param("since") Instant since);
}
