package fr.trollgun.optimiam.product.application;

import fr.trollgun.optimiam.common.dto.PageResponse;
import fr.trollgun.optimiam.common.exception.ResourceNotFoundException;
import fr.trollgun.optimiam.product.api.dto.CreateProductRequest;
import fr.trollgun.optimiam.product.api.dto.ProductResponse;
import fr.trollgun.optimiam.product.api.dto.UpdateProductRequest;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.product.domain.ProductRepository;
import fr.trollgun.optimiam.product.domain.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private Category category;
    private UUID productId;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = Category.builder()
                .id(categoryId)
                .name("Fruits & Légumes")
                .build();

        productId = UUID.randomUUID();
        product = Product.builder()
                .id(productId)
                .name("Tomate")
                .barcode("123456789")
                .defaultUnit(Unit.KG)
                .category(category)
                .averageShelfLifeDays(5)
                .deleted(false)
                .build();
    }

    @Test
    @DisplayName("Doit lister les produits paginés")
    void shouldReturnPaginatedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        when(productRepository.findByDeletedFalse(pageable)).thenReturn(new PageImpl<>(List.of(product), pageable, 1));

        PageResponse<ProductResponse> result = productService.getProducts(null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Tomate");
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("Doit récupérer un produit par son ID")
    void shouldGetProductById() {
        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.of(product));

        ProductResponse result = productService.getProductById(productId);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Tomate");
    }

    @Test
    @DisplayName("Doit lever une exception si le produit est introuvable")
    void shouldThrowWhenProductNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(productRepository.findByIdAndDeletedFalse(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Doit créer un nouveau produit")
    void shouldCreateProduct() {
        CreateProductRequest request = CreateProductRequest.builder()
                .name("Courgette")
                .barcode("987654321")
                .defaultUnit(Unit.KG)
                .categoryId(categoryId)
                .averageShelfLifeDays(7)
                .build();

        when(categoryService.getCategoryEntity(categoryId)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        ProductResponse result = productService.createProduct(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Courgette");
        assertThat(result.getCategory().getName()).isEqualTo("Fruits & Légumes");
    }

    @Test
    @DisplayName("Doit supprimer un produit en soft delete")
    void shouldSoftDeleteProduct() {
        when(productRepository.findByIdAndDeletedFalse(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

        productService.deleteProduct(productId);

        assertThat(product.isDeleted()).isTrue();
        assertThat(product.getDeletedAt()).isNotNull();
        verify(productRepository).save(product);
    }
}
