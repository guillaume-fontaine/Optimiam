package fr.trollgun.optimiam.recipe.api;

import fr.trollgun.optimiam.common.dto.PageResponse;
import fr.trollgun.optimiam.recipe.api.dto.CreateRecipeRequest;
import fr.trollgun.optimiam.recipe.api.dto.RecipeResponse;
import fr.trollgun.optimiam.recipe.api.dto.UpdateRecipeRequest;
import fr.trollgun.optimiam.recipe.application.RecipeService;
import fr.trollgun.optimiam.recipe.domain.Difficulty;
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
@RequestMapping("/api/v1/recipes")
@RequiredArgsConstructor
@Tag(name = "Recettes", description = "Gestion du catalogue de recettes de cuisine et informations nutritionnelles")
public class RecipeController {

    private final RecipeService recipeService;

    @GetMapping
    @Operation(summary = "Rechercher et lister les recettes avec pagination et filtres")
    public ResponseEntity<PageResponse<RecipeResponse>> getRecipes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String tag,
            @RequestParam(required = false) Integer maxPrepTime,
            @RequestParam(required = false) Difficulty difficulty,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(recipeService.getRecipes(query, tag, maxPrepTime, difficulty, pageable));
    }

    @GetMapping("/all")
    @Operation(summary = "Lister toutes les recettes sans pagination")
    public ResponseEntity<List<RecipeResponse>> getAllRecipes() {
        return ResponseEntity.ok(recipeService.getAllRecipes());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails complets d'une recette avec ingrédients, étapes et nutrition")
    public ResponseEntity<RecipeResponse> getRecipeById(@PathVariable UUID id) {
        return ResponseEntity.ok(recipeService.getRecipeById(id));
    }

    @PostMapping
    @Operation(summary = "Créer une nouvelle recette de cuisine")
    public ResponseEntity<RecipeResponse> createRecipe(@Valid @RequestBody CreateRecipeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(recipeService.createRecipe(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Mettre à jour une recette existante")
    public ResponseEntity<RecipeResponse> updateRecipe(@PathVariable UUID id, @Valid @RequestBody UpdateRecipeRequest request) {
        return ResponseEntity.ok(recipeService.updateRecipe(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une recette")
    public ResponseEntity<Void> deleteRecipe(@PathVariable UUID id) {
        recipeService.deleteRecipe(id);
        return ResponseEntity.noContent().build();
    }
}
