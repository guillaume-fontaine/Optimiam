package fr.trollgun.optimiam.product.api;

import fr.trollgun.optimiam.common.dto.PageResponse;
import fr.trollgun.optimiam.product.api.dto.CreateProductRequest;
import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.product.api.dto.UpdateProductRequest;
import fr.trollgun.optimiam.product.application.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Produits", description = "Gestion du catalogue des produits alimentaires")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "Rechercher et lister les produits avec pagination et filtres")
    public ResponseEntity<PageResponse<ProductResponse>> getProducts(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(productService.getProducts(query, categoryId, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Lister tous les produits actifs sans pagination")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir un produit par son identifiant")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/barcode/{barcode}")
    @Operation(summary = "Rechercher un produit par code-barres")
    public ResponseEntity<ProductResponse> getProductByBarcode(@PathVariable String barcode) {
        return ResponseEntity.ok(productService.getProductByBarcode(barcode));
    }

    @PostMapping
    @Operation(summary = "Créer un nouveau produit dans le catalogue")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour un produit existant")
    public ResponseEntity<ProductResponse> updateProduct(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un produit du catalogue (soft delete)")
    public ResponseEntity<Void> deleteProduct(@PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
