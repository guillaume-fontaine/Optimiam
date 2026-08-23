package fr.trollgun.optimiam.shopping.api.dto;

import fr.trollgun.optimiam.shopping.domain.ShoppingList;
import fr.trollgun.optimiam.shopping.domain.ShoppingListStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
@Builder
public class ShoppingListResponse {
    private UUID id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private ShoppingListStatus status;
    private String statusLabel;
    private int totalItemsCount;
    private int checkedItemsCount;
    private int completionPercentage;
    private List<ShoppingListItemResponse> items;
    private Instant createdAt;
    private Instant updatedAt;

    public static ShoppingListResponse from(ShoppingList list) {
        if (list == null) return null;
        List<ShoppingListItemResponse> itemResponses = (list.getItems() != null)
                ? list.getItems().stream().map(ShoppingListItemResponse::from).collect(Collectors.toList())
                : Collections.emptyList();

        int total = itemResponses.size();
        int checked = (int) itemResponses.stream().filter(ShoppingListItemResponse::isChecked).count();
        int pct = total > 0 ? (checked * 100) / total : 0;

        return ShoppingListResponse.builder()
                .id(list.getId())
                .name(list.getName())
                .startDate(list.getStartDate())
                .endDate(list.getEndDate())
                .status(list.getStatus())
                .statusLabel(list.getStatus() != null ? list.getStatus().getLabel() : null)
                .totalItemsCount(total)
                .checkedItemsCount(checked)
                .completionPercentage(pct)
                .items(itemResponses)
                .createdAt(list.getCreatedAt())
                .updatedAt(list.getUpdatedAt())
                .build();
    }
}
