package fr.trollgun.optimiam.product.api.dto;

import fr.trollgun.optimiam.product.domain.Unit;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductRequest {

    @NotBlank(message = "Le nom du produit est obligatoire")
    @Size(max = 150, message = "Le nom ne peut pas dépasser 150 caractères")
    private String name;

    @Size(max = 100, message = "Le code-barres ne peut pas dépasser 100 caractères")
    private String barcode;

    @NotNull(message = "L'unité par défaut est obligatoire")
    private Unit defaultUnit;

    private UUID categoryId;

    @Min(value = 1, message = "La durée de conservation doit être d'au moins 1 jour")
    private Integer averageShelfLifeDays;

    @Size(max = 500, message = "L'URL de l'image ne peut pas dépasser 500 caractères")
    private String imageUrl;
}
