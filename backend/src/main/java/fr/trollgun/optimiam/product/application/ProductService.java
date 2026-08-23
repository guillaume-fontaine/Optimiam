package fr.trollgun.optimiam.product.application;

import fr.trollgun.optimiam.common.dto.PageResponse;
import fr.trollgun.optimiam.common.exception.ErrorCode;
import fr.trollgun.optimiam.common.exception.ResourceNotFoundException;
import fr.trollgun.optimiam.product.api.dto.CreateProductRequest;
import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.product.api.dto.UpdateProductRequest;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> getProducts(String query, UUID categoryId, Pageable pageable) {
        Page<Product> productPage;

        boolean hasQuery = query != null && !query.trim().isEmpty();
        boolean hasCategory = categoryId != null;

        if (hasQuery && hasCategory) {
            productPage = productRepository.findByDeletedFalseAndCategoryIdAndNameContainingIgnoreCase(categoryId, query.trim(), pageable);
        } else if (hasQuery) {
            productPage = productRepository.findByDeletedFalseAndNameContainingIgnoreCase(query.trim(), pageable);
        } else if (hasCategory) {
            productPage = productRepository.findByDeletedFalseAndCategoryId(categoryId, pageable);
        } else {
            productPage = productRepository.findByDeletedFalse(pageable);
        }

        return PageResponse.from(productPage.map(ProductResponse::from));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> getAllProducts() {
        return productRepository.findByDeletedFalse().stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(UUID id) {
        return ProductResponse.from(getProductEntity(id));
    }

    @Transactional(readOnly = true)
    public Product getProductEntity(UUID id) {
        return productRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Produit introuvable avec l'identifiant: " + id, ErrorCode.PRODUCT_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductByBarcode(String barcode) {
        Product product = productRepository.findByBarcodeAndDeletedFalse(barcode)
                .orElseThrow(() -> new ResourceNotFoundException("Aucun produit associé au code-barres: " + barcode, ErrorCode.PRODUCT_NOT_FOUND));
        return ProductResponse.from(product);
    }

    @Transactional
    public ProductResponse createProduct(CreateProductRequest request) {
        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryEntity(request.getCategoryId());
        }

        Product product = Product.builder()
                .name(request.getName().trim())
                .barcode(request.getBarcode())
                .defaultUnit(request.getDefaultUnit())
                .category(category)
                .averageShelfLifeDays(request.getAverageShelfLifeDays())
                .imageUrl(request.getImageUrl())
                .deleted(false)
                .build();

        Product saved = productRepository.save(product);
        log.info("Produit créé: {} [{}]", saved.getName(), saved.getId());
        return ProductResponse.from(saved);
    }

    @Transactional
    public ProductResponse updateProduct(UUID id, UpdateProductRequest request) {
        Product product = getProductEntity(id);

        Category category = null;
        if (request.getCategoryId() != null) {
            category = categoryService.getCategoryEntity(request.getCategoryId());
        }

        product.setName(request.getName().trim());
        product.setBarcode(request.getBarcode());
        product.setDefaultUnit(request.getDefaultUnit());
        product.setCategory(category);
        product.setAverageShelfLifeDays(request.getAverageShelfLifeDays());
        product.setImageUrl(request.getImageUrl());

        Product updated = productRepository.save(product);
        log.info("Produit mis à jour: {} [{}]", updated.getName(), updated.getId());
        return ProductResponse.from(updated);
    }

    @Transactional
    public void deleteProduct(UUID id) {
        Product product = getProductEntity(id);
        product.setDeleted(true);
        product.setDeletedAt(Instant.now());
        productRepository.save(product);
        log.info("Produit marqué comme supprimé (soft delete): {} [{}]", product.getName(), id);
    }
}
