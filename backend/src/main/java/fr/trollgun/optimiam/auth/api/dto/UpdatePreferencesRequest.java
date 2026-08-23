package fr.trollgun.optimiam.auth.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePreferencesRequest {
    private Integer maxPrepTimeMinutes;
    private Boolean vegetarian;
    private Boolean vegan;
    private Boolean glutenFree;
}
