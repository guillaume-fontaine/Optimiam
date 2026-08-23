package fr.trollgun.optimiam.product.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateCategoryRequest {

    @NotBlank(message = "Le nom de la catégorie est obligatoire")
    @Size(max = 100, message = "Le nom ne peut pas dépasser 100 caractères")
    private String name;

    @Size(max = 50, message = "L'icône ne peut pas dépasser 50 caractères")
    private String icon;

    @Size(max = 30, message = "La couleur ne peut pas dépasser 30 caractères")
    private String color;

    @Size(max = 500, message = "La description ne peut pas dépasser 500 caractères")
    private String description;
}
