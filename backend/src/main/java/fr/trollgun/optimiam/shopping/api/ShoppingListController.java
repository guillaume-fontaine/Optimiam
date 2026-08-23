package fr.trollgun.optimiam.shopping.api;

import fr.trollgun.optimiam.shopping.api.dto.*;
import fr.trollgun.optimiam.shopping.application.ShoppingListService;
import fr.trollgun.optimiam.shopping.domain.ShoppingListStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-lists")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Génération intelligente de liste de courses et transfert d'achats en stock")
public class ShoppingListController {

    private final ShoppingListService shoppingListService;

    @GetMapping
    @Operation(summary = "Lister toutes les listes de courses")
    public ResponseEntity<List<ShoppingListResponse>> getAllShoppingLists(@RequestParam(required = false) ShoppingListStatus status) {
        return ResponseEntity.ok(shoppingListService.getAllShoppingLists(status));
    }

    @GetMapping("/active")
    @Operation(summary = "Obtenir la liste de courses active courante")
    public ResponseEntity<ShoppingListResponse> getActiveShoppingList() {
        return ResponseEntity.ok(shoppingListService.getActiveShoppingList());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir une liste de courses par son identifiant")
    public ResponseEntity<ShoppingListResponse> getShoppingListById(@PathVariable UUID id) {
        return ResponseEntity.ok(shoppingListService.getShoppingListById(id));
    }

    @PostMapping("/generate")
    @Operation(summary = "Générer automatiquement une liste de courses à partir des repas planifiés")
    public ResponseEntity<ShoppingListResponse> generateFromPlanning(@RequestBody(required = false) GenerateShoppingListRequest request) {
        GenerateShoppingListRequest req = request != null ? request : GenerateShoppingListRequest.builder().build();
        return ResponseEntity.status(HttpStatus.CREATED).body(shoppingListService.generateFromPlanning(req));
    }

    @PostMapping("/{id}/items")
    @Operation(summary = "Ajouter manuellement un article libre à la liste")
    public ResponseEntity<ShoppingListResponse> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody AddShoppingItemRequest request
    ) {
        return ResponseEntity.ok(shoppingListService.addItemToList(id, request));
    }

    @PutMapping("/{id}/items/{itemId}")
    @Operation(summary = "Mettre à jour un article (quantité, coché/décoché)")
    public ResponseEntity<ShoppingListResponse> updateItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @RequestBody UpdateShoppingItemRequest request
    ) {
        return ResponseEntity.ok(shoppingListService.updateItem(id, itemId, request));
    }

    @PostMapping("/{id}/validate-purchases")
    @Operation(summary = "Valider les achats cochés et créer automatiquement les entrées de stock correspondantes")
    public ResponseEntity<ShoppingListResponse> validatePurchases(@PathVariable UUID id) {
        return ResponseEntity.ok(shoppingListService.validatePurchases(id));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer une liste de courses")
    public ResponseEntity<Void> deleteShoppingList(@PathVariable UUID id) {
        shoppingListService.deleteShoppingList(id);
        return ResponseEntity.noContent().build();
    }
}
