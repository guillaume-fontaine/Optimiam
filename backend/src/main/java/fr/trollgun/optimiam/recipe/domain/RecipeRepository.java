package fr.trollgun.optimiam.recipe.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RecipeRepository extends JpaRepository<Recipe, UUID> {

    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN r.tags t WHERE " +
            "(:query IS NULL OR LOWER(r.name) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(r.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
            "(:difficulty IS NULL OR r.difficulty = :difficulty) AND " +
            "(:maxPrepTime IS NULL OR r.preparationTimeMinutes <= :maxPrepTime) AND " +
            "(:tag IS NULL OR t = :tag)")
    Page<Recipe> searchRecipes(
            @Param("query") String query,
            @Param("difficulty") Difficulty difficulty,
            @Param("maxPrepTime") Integer maxPrepTime,
            @Param("tag") String tag,
            Pageable pageable
    );

    @Query("SELECT r FROM Recipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.product WHERE r.id = :id")
    Optional<Recipe> findByIdWithIngredients(@Param("id") UUID id);

    @Query("SELECT DISTINCT r FROM Recipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.product")
    List<Recipe> findAllWithIngredients();
}
