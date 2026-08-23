package fr.trollgun.optimiam.recipe.api.dto;

import fr.trollgun.optimiam.nutrition.domain.Nutrition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NutritionDto {
    private BigDecimal calories;
    private BigDecimal protein;
    private BigDecimal carbohydrates;
    private BigDecimal fat;
    private BigDecimal fiber;
    private BigDecimal salt;

    public static NutritionDto from(Nutrition nutrition) {
        if (nutrition == null) return null;
        return NutritionDto.builder()
                .calories(nutrition.getCalories())
                .protein(nutrition.getProtein())
                .carbohydrates(nutrition.getCarbohydrates())
                .fat(nutrition.getFat())
                .fiber(nutrition.getFiber())
                .salt(nutrition.getSalt())
                .build();
    }

    public Nutrition toEntity() {
        return Nutrition.builder()
                .calories(this.calories)
                .protein(this.protein)
                .carbohydrates(this.carbohydrates)
                .fat(this.fat)
                .fiber(this.fiber)
                .salt(this.salt)
                .build();
    }
}
