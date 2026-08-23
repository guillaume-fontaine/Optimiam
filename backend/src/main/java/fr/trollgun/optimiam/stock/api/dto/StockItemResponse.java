package fr.trollgun.optimiam.stock.api.dto;

import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.product.domain.Unit;
import fr.trollgun.optimiam.stock.domain.Location;
import fr.trollgun.optimiam.stock.domain.StockItem;
import fr.trollgun.optimiam.stock.domain.StockStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Builder
public class StockItemResponse {
    private UUID id;
    private ProductResponse product;
    private BigDecimal quantity;
    private Unit unit;
    private String unitLabel;
    private String unitSymbol;
    private LocalDate entryDate;
    private LocalDate expirationDate;
    private Long daysUntilExpiration;
    private Location location;
    private String locationLabel;
    private String locationIcon;
    private StockStatus status;
    private String statusLabel;
    private Long version;
    private Instant createdAt;
    private Instant updatedAt;

    public static StockItemResponse from(StockItem item, LocalDate today) {
        if (item == null) return null;
        StockStatus dynStatus = item.getDynamicStatus(today);
        return StockItemResponse.builder()
                .id(item.getId())
                .product(ProductResponse.from(item.getProduct()))
                .quantity(item.getQuantity())
                .unit(item.getUnit())
                .unitLabel(item.getUnit() != null ? item.getUnit().getLabel() : null)
                .unitSymbol(item.getUnit() != null ? item.getUnit().getSymbol() : null)
                .entryDate(item.getEntryDate())
                .expirationDate(item.getExpirationDate())
                .daysUntilExpiration(item.getDaysUntilExpiration(today))
                .location(item.getLocation())
                .locationLabel(item.getLocation() != null ? item.getLocation().getLabel() : null)
                .locationIcon(item.getLocation() != null ? item.getLocation().getIcon() : null)
                .status(dynStatus)
                .statusLabel(dynStatus != null ? dynStatus.getLabel() : null)
                .version(item.getVersion())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }
}
