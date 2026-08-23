package fr.trollgun.optimiam.planning.api;

import fr.trollgun.optimiam.planning.api.dto.CreateMealPlanRequest;
import fr.trollgun.optimiam.planning.api.dto.MealPlanResponse;
import fr.trollgun.optimiam.planning.api.dto.UpdateMealPlanRequest;
import fr.trollgun.optimiam.planning.application.MealPlanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/planning")
@RequiredArgsConstructor
@Tag(name = "Planning", description = "Planification des repas et synchronisation avec les stocks")
public class MealPlanController {

    private final MealPlanService mealPlanService;

    @GetMapping
    @Operation(summary = "Lister les repas planifiés pour une plage de dates (semaine ou mois)")
    public ResponseEntity<List<MealPlanResponse>> getMealPlans(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return ResponseEntity.ok(mealPlanService.getMealPlans(startDate, endDate));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtenir les détails d'un repas planifié")
    public ResponseEntity<MealPlanResponse> getMealPlanById(@PathVariable UUID id) {
        return ResponseEntity.ok(mealPlanService.getMealPlanById(id));
    }

    @PostMapping
    @Operation(summary = "Planifier un nouveau repas")
    public ResponseEntity<MealPlanResponse> createMealPlan(@Valid @RequestBody CreateMealPlanRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mealPlanService.createMealPlan(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Modifier un repas planifié")
    public ResponseEntity<MealPlanResponse> updateMealPlan(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMealPlanRequest request
    ) {
        return ResponseEntity.ok(mealPlanService.updateMealPlan(id, request));
    }

    @PostMapping("/{id}/cook")
    @Operation(summary = "Valider un repas comme cuisiné et déduire optionnellement les stocks")
    public ResponseEntity<MealPlanResponse> markAsCooked(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "true") boolean deductStock
    ) {
        return ResponseEntity.ok(mealPlanService.markAsCooked(id, deductStock));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Supprimer un repas du planning")
    public ResponseEntity<Void> deleteMealPlan(@PathVariable UUID id) {
        mealPlanService.deleteMealPlan(id);
        return ResponseEntity.noContent().build();
    }
}
