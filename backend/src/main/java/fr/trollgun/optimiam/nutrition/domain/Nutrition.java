package fr.trollgun.optimiam.nutrition.domain;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.math.BigDecimal;

@Embeddable
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Nutrition {

    /**
     * Calories par portion (kcal)
     */
    private BigDecimal calories;

    /**
     * Protéines par portion (g)
     */
    private BigDecimal protein;

    /**
     * Glucides par portion (g)
     */
    private BigDecimal carbohydrates;

    /**
     * Lipides par portion (g)
     */
    private BigDecimal fat;

    /**
     * Fibres par portion (g)
     */
    private BigDecimal fiber;

    /**
     * Sel par portion (g)
     */
    private BigDecimal salt;
}
