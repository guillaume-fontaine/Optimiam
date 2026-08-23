package fr.trollgun.optimiam.recommendation.api;

import fr.trollgun.optimiam.recommendation.api.dto.RecommendationRequest;
import fr.trollgun.optimiam.recommendation.api.dto.RecommendationResponse;
import fr.trollgun.optimiam.recommendation.application.RecommendationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
@Tag(name = "Recommandations", description = "Moteur intelligent de recommandation de recettes selon le stock et les péremptions")
public class RecommendationController {

    private final RecommendationService recommendationService;

    @PostMapping
    @Operation(summary = "Générer les recommandations de recettes personnalisées selon le stock et les critères")
    public ResponseEntity<List<RecommendationResponse>> getRecommendations(@RequestBody(required = false) RecommendationRequest request) {
        RecommendationRequest req = request != null ? request : RecommendationRequest.builder().build();
        return ResponseEntity.ok(recommendationService.getRecommendations(req));
    }

    @GetMapping("/daily")
    @Operation(summary = "Obtenir les meilleures suggestions de recettes anti-gaspi du jour")
    public ResponseEntity<List<RecommendationResponse>> getDailyRecommendations(@RequestParam(defaultValue = "3") int limit) {
        return ResponseEntity.ok(recommendationService.getDailyTopRecommendations(limit));
    }
}
