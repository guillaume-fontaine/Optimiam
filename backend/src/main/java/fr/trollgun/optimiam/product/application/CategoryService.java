package fr.trollgun.optimiam.product.application;

import fr.trollgun.optimiam.common.exception.ConflictException;
import fr.trollgun.optimiam.common.exception.ErrorCode;
import fr.trollgun.optimiam.common.exception.ResourceNotFoundException;
import fr.trollgun.optimiam.product.api.dto.CategoryResponse;
import fr.trollgun.optimiam.product.api.dto.CreateCategoryRequest;
import fr.trollgun.optimiam.product.api.dto.UpdateCategoryRequest;
import fr.trollgun.optimiam.product.domain.Category;
import fr.trollgun.optimiam.product.domain.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(UUID id) {
        return CategoryResponse.from(getCategoryEntity(id));
    }

    @Transactional(readOnly = true)
    public Category getCategoryEntity(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Catégorie introuvable avec l'identifiant: " + id, ErrorCode.CATEGORY_NOT_FOUND));
    }

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ConflictException("Une catégorie avec ce nom existe déjà: " + request.getName(), ErrorCode.CONFLICT);
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .icon(request.getIcon())
                .color(request.getColor())
                .description(request.getDescription())
                .build();

        Category saved = categoryRepository.save(category);
        log.info("Catégorie créée: {} [{}]", saved.getName(), saved.getId());
        return CategoryResponse.from(saved);
    }

    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        Category category = getCategoryEntity(id);

        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
            throw new ConflictException("Une autre catégorie avec ce nom existe déjà: " + request.getName(), ErrorCode.CONFLICT);
        }

        category.setName(request.getName().trim());
        category.setIcon(request.getIcon());
        category.setColor(request.getColor());
        category.setDescription(request.getDescription());

        Category updated = categoryRepository.save(category);
        log.info("Catégorie mise à jour: {} [{}]", updated.getName(), updated.getId());
        return CategoryResponse.from(updated);
    }

    @Transactional
    public void deleteCategory(UUID id) {
        Category category = getCategoryEntity(id);
        categoryRepository.delete(category);
        log.info("Catégorie supprimée: {} [{}]", category.getName(), id);
    }
}
