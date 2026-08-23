package fr.trollgun.optimiam.product.api.dto;

import fr.trollgun.optimiam.product.domain.Category;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
    private String icon;
    private String color;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;

    public static CategoryResponse from(Category category) {
        if (category == null) return null;
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .color(category.getColor())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
