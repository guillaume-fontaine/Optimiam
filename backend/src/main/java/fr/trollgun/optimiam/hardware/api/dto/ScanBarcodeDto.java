package fr.trollgun.optimiam.hardware.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScanBarcodeDto {
    @NotBlank(message = "Le code-barres ne peut pas être vide")
    private String barcode;
}
