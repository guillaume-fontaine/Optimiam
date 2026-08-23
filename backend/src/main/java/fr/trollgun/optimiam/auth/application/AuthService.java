package fr.trollgun.optimiam.auth.application;

import fr.trollgun.optimiam.auth.api.dto.*;
import fr.trollgun.optimiam.auth.infrastructure.JwtService;
import fr.trollgun.optimiam.common.exception.ConflictException;
import fr.trollgun.optimiam.common.exception.ErrorCode;
import fr.trollgun.optimiam.common.exception.ResourceNotFoundException;
import fr.trollgun.optimiam.user.domain.Role;
import fr.trollgun.optimiam.user.domain.User;
import fr.trollgun.optimiam.user.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Un compte existe déjà avec l'adresse email : " + request.getEmail(), ErrorCode.CONFLICT);
        }

        User user = User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .username(request.getUsername().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.ROLE_USER)
                .maxPrepTimeMinutes(30)
                .build();

        User saved = userRepository.save(user);
        log.info("Nouvel utilisateur inscrit : {} [{}]", saved.getEmail(), saved.getId());

        String token = jwtService.generateToken(saved);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(UserDto.from(saved))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().toLowerCase().trim();
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé", ErrorCode.USER_NOT_FOUND));

        String token = jwtService.generateToken(user);
        log.info("Connexion réussie pour l'utilisateur : {}", email);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .user(UserDto.from(user))
                .build();
    }

    @Transactional(readOnly = true)
    public UserDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé", ErrorCode.USER_NOT_FOUND));
        return UserDto.from(user);
    }

    @Transactional
    public UserDto updatePreferences(String email, UpdatePreferencesRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé", ErrorCode.USER_NOT_FOUND));

        if (request.getMaxPrepTimeMinutes() != null) {
            user.setMaxPrepTimeMinutes(request.getMaxPrepTimeMinutes());
        }
        if (request.getVegetarian() != null) {
            user.setVegetarian(request.getVegetarian());
        }
        if (request.getVegan() != null) {
            user.setVegan(request.getVegan());
        }
        if (request.getGlutenFree() != null) {
            user.setGlutenFree(request.getGlutenFree());
        }

        User updated = userRepository.save(user);
        log.info("Préférences mises à jour pour : {}", email);
        return UserDto.from(updated);
    }
}
