package fr.trollgun.optimiam.shopping.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateShoppingListRequest {
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
}
