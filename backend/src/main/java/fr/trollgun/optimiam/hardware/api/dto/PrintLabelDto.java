package fr.trollgun.optimiam.hardware.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrintLabelDto {
    private String productName;
    private String barcode;
    private LocalDate entryDate;
    private LocalDate expirationDate;
    private String quantityWithUnit;
    private String location;
}
