package fr.trollgun.optimiam.auth.api.dto;

import fr.trollgun.optimiam.user.domain.Role;
import fr.trollgun.optimiam.user.domain.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    private UUID id;
    private String email;
    private String username;
    private Role role;
    private Integer maxPrepTimeMinutes;
    private boolean vegetarian;
    private boolean vegan;
    private boolean glutenFree;

    public static UserDto from(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getDisplayName())
                .role(user.getRole())
                .maxPrepTimeMinutes(user.getMaxPrepTimeMinutes())
                .vegetarian(user.isVegetarian())
                .vegan(user.isVegan())
                .glutenFree(user.isGlutenFree())
                .build();
    }
}
