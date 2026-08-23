package fr.trollgun.optimiam.product.api.dto;

import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.Unit;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class ProductResponse {
    private UUID id;
    private String name;
    private String barcode;
    private Unit defaultUnit;
    private String unitLabel;
    private String unitSymbol;
    private CategoryResponse category;
    private Integer averageShelfLifeDays;
    private String imageUrl;
    private Instant createdAt;
    private Instant updatedAt;

    public static ProductResponse from(Product product) {
        if (product == null) return null;
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .barcode(product.getBarcode())
                .defaultUnit(product.getDefaultUnit())
                .unitLabel(product.getDefaultUnit() != null ? product.getDefaultUnit().getLabel() : null)
                .unitSymbol(product.getDefaultUnit() != null ? product.getDefaultUnit().getSymbol() : null)
                .category(CategoryResponse.from(product.getCategory()))
                .averageShelfLifeDays(product.getAverageShelfLifeDays())
                .imageUrl(product.getImageUrl())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
