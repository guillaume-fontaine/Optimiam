package fr.trollgun.optimiam.stock.domain;

import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Entity
@Table(name = "stock_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Unit unit;

    @Column(nullable = false)
    private LocalDate entryDate;

    private LocalDate expirationDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Location location = Location.FRIDGE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private StockStatus status = StockStatus.AVAILABLE;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;

    public StockStatus getDynamicStatus(LocalDate today) {
        if (quantity.compareTo(BigDecimal.ZERO) <= 0) {
            return StockStatus.CONSUMED;
        }
        if (expirationDate == null) {
            return StockStatus.AVAILABLE;
        }
        if (expirationDate.isBefore(today)) {
            return StockStatus.EXPIRED;
        }
        if (today.until(expirationDate, ChronoUnit.DAYS) <= 3) {
            return StockStatus.EXPIRING_SOON;
        }
        return StockStatus.AVAILABLE;
    }

    public Long getDaysUntilExpiration(LocalDate today) {
        if (expirationDate == null) {
            return null;
        }
        return ChronoUnit.DAYS.between(today, expirationDate);
    }
}
