package fr.trollgun.optimiam.recipe.application;

import fr.trollgun.optimiam.common.dto.PageResponse;
import fr.trollgun.optimiam.common.exception.ErrorCode;
import fr.trollgun.optimiam.common.exception.ResourceNotFoundException;
import fr.trollgun.optimiam.product.application.ProductService;
import fr.trollgun.optimiam.product.domain.Product;
import fr.trollgun.optimiam.recipe.api.dto.CreateRecipeIngredientRequest;
import fr.trollgun.optimiam.recipe.api.dto.CreateRecipeRequest;
import fr.trollgun.optimiam.recipe.api.dto.CreateRecipeStepRequest;
import fr.trollgun.optimiam.recipe.api.dto.RecipeResponse;
import fr.trollgun.optimiam.recipe.api.dto.UpdateRecipeRequest;
import fr.trollgun.optimiam.recipe.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final ProductService productService;

    @Transactional(readOnly = true)
    public PageResponse<RecipeResponse> getRecipes(String query, String tag, Integer maxPrepTime, Difficulty difficulty, Pageable pageable) {
        String queryPattern = (query != null && !query.trim().isEmpty()) ? "%" + query.trim().toLowerCase() + "%" : null;
        String cleanTag = (tag != null && !tag.trim().isEmpty()) ? tag.trim() : null;

        return PageResponse.from(
                recipeRepository.searchRecipes(queryPattern, difficulty, maxPrepTime, cleanTag, pageable)
                        .map(RecipeResponse::from)
        );
    }

    @Transactional(readOnly = true)
    public List<RecipeResponse> getAllRecipes() {
        return recipeRepository.findAllWithIngredients().stream()
                .map(RecipeResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RecipeResponse getRecipeById(UUID id) {
        return RecipeResponse.from(getRecipeEntity(id));
    }

    @Transactional(readOnly = true)
    public Recipe getRecipeEntity(UUID id) {
        return recipeRepository.findByIdWithIngredients(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recette introuvable avec l'identifiant: " + id, ErrorCode.RECIPE_NOT_FOUND));
    }

    @Transactional
    public RecipeResponse createRecipe(CreateRecipeRequest request) {
        Recipe recipe = Recipe.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .preparationTimeMinutes(request.getPreparationTimeMinutes())
                .cookingTimeMinutes(request.getCookingTimeMinutes())
                .difficulty(request.getDifficulty())
                .servings(request.getServings())
                .imageUrl(request.getImageUrl())
                .nutrition(request.getNutrition() != null ? request.getNutrition().toEntity() : null)
                .tags(request.getTags() != null ? request.getTags() : null)
                .build();

        if (request.getIngredients() != null) {
            for (CreateRecipeIngredientRequest ingReq : request.getIngredients()) {
                Product product = productService.getProductEntity(ingReq.getProductId());
                RecipeIngredient ingredient = RecipeIngredient.builder()
                        .product(product)
                        .quantity(ingReq.getQuantity())
                        .unit(ingReq.getUnit())
                        .optional(ingReq.isOptional())
                        .build();
                recipe.addIngredient(ingredient);
            }
        }

        if (request.getSteps() != null) {
            for (CreateRecipeStepRequest stepReq : request.getSteps()) {
                RecipeStep step = RecipeStep.builder()
                        .stepNumber(stepReq.getStepNumber())
                        .instruction(stepReq.getInstruction())
                        .durationMinutes(stepReq.getDurationMinutes())
                        .build();
                recipe.addStep(step);
            }
        }

        Recipe saved = recipeRepository.save(recipe);
        log.info("Recette créée: {} avec {} ingrédients et {} étapes [id={}]",
                saved.getName(), saved.getIngredients().size(), saved.getSteps().size(), saved.getId());

        return RecipeResponse.from(saved);
    }

    @Transactional
    public RecipeResponse updateRecipe(UUID id, UpdateRecipeRequest request) {
        Recipe recipe = getRecipeEntity(id);

        recipe.setName(request.getName().trim());
        recipe.setDescription(request.getDescription());
        recipe.setPreparationTimeMinutes(request.getPreparationTimeMinutes());
        recipe.setCookingTimeMinutes(request.getCookingTimeMinutes());
        recipe.setDifficulty(request.getDifficulty());
        recipe.setServings(request.getServings());
        recipe.setImageUrl(request.getImageUrl());
        recipe.setNutrition(request.getNutrition() != null ? request.getNutrition().toEntity() : null);
        recipe.setTags(request.getTags());

        // Mise à jour des ingrédients
        recipe.getIngredients().clear();
        if (request.getIngredients() != null) {
            for (CreateRecipeIngredientRequest ingReq : request.getIngredients()) {
                Product product = productService.getProductEntity(ingReq.getProductId());
                RecipeIngredient ingredient = RecipeIngredient.builder()
                        .product(product)
                        .quantity(ingReq.getQuantity())
                        .unit(ingReq.getUnit())
                        .optional(ingReq.isOptional())
                        .build();
                recipe.addIngredient(ingredient);
            }
        }

        // Mise à jour des étapes
        recipe.getSteps().clear();
        if (request.getSteps() != null) {
            for (CreateRecipeStepRequest stepReq : request.getSteps()) {
                RecipeStep step = RecipeStep.builder()
                        .stepNumber(stepReq.getStepNumber())
                        .instruction(stepReq.getInstruction())
                        .durationMinutes(stepReq.getDurationMinutes())
                        .build();
                recipe.addStep(step);
            }
        }

        Recipe updated = recipeRepository.save(recipe);
        log.info("Recette mise à jour: {} [id={}]", updated.getName(), updated.getId());

        return RecipeResponse.from(updated);
    }

    @Transactional
    public void deleteRecipe(UUID id) {
        Recipe recipe = getRecipeEntity(id);
        recipeRepository.delete(recipe);
        log.info("Recette supprimée: {} [id={}]", recipe.getName(), id);
    }
}
