package fr.trollgun.optimiam.product.application;

import fr.trollgun.optimiam.common.exception.ConflictException;
import fr.trollgun.optimiam.common.exception.ResourceNotFoundException;
import fr.trollgun.optimiam.product.api.dto.CategoryResponse;
import fr.trollgun.optimiam.product.api.dto.CreateCategoryRequest;
import fr.trollgun.optimiam.product.api.dto.UpdateCategoryRequest;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;
    private UUID categoryId;

    @BeforeEach
    void setUp() {
        categoryId = UUID.randomUUID();
        category = Category.builder()
                .id(categoryId)
                .name("Fruits & Légumes")
                .icon("eco")
                .color("#16a34a")
                .description("Fruits et légumes frais")
                .build();
    }

    @Test
    @DisplayName("Doit retourner toutes les catégories")
    void shouldReturnAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(category));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Fruits & Légumes");
        verify(categoryRepository).findAll();
    }

    @Test
    @DisplayName("Doit retourner une catégorie par ID")
    void shouldReturnCategoryById() {
        when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(category));

        CategoryResponse result = categoryService.getCategoryById(categoryId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(categoryId);
    }

    @Test
    @DisplayName("Doit lever une exception si la catégorie est introuvable")
    void shouldThrowWhenCategoryNotFound() {
        UUID unknownId = UUID.randomUUID();
        when(categoryRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Doit créer une nouvelle catégorie")
    void shouldCreateCategory() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Boissons")
                .icon("local_drink")
                .color("#0284c7")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Boissons")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category c = invocation.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CategoryResponse result = categoryService.createCategory(request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Boissons");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    @DisplayName("Doit lever un conflit si une catégorie avec le même nom existe déjà")
    void shouldThrowConflictWhenCategoryNameExists() {
        CreateCategoryRequest request = CreateCategoryRequest.builder()
                .name("Fruits & Légumes")
                .build();

        when(categoryRepository.existsByNameIgnoreCase("Fruits & Légumes")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(ConflictException.class);

        verify(categoryRepository, never()).save(any(Category.class));
    }
}
