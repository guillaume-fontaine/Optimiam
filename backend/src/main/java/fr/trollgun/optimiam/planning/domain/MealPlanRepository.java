package fr.trollgun.optimiam.planning.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MealPlanRepository extends JpaRepository<MealPlan, UUID> {

    @Query("SELECT m FROM MealPlan m JOIN FETCH m.recipe r LEFT JOIN FETCH r.ingredients i LEFT JOIN FETCH i.product " +
            "WHERE m.date BETWEEN :startDate AND :endDate ORDER BY m.date ASC, m.mealType ASC")
    List<MealPlan> findByDateBetweenWithDetails(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<MealPlan> findByDateOrderByMealTypeAsc(LocalDate date);

    List<MealPlan> findByStatus(MealPlanStatus status);
}
