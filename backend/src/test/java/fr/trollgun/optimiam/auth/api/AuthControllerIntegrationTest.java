package fr.trollgun.optimiam.auth.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.trollgun.optimiam.auth.api.dto.LoginRequest;
import fr.trollgun.optimiam.auth.api.dto.RegisterRequest;
import fr.trollgun.optimiam.auth.api.dto.UpdatePreferencesRequest;
import fr.trollgun.optimiam.user.domain.Role;
import fr.trollgun.optimiam.user.domain.User;
import fr.trollgun.optimiam.user.domain.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
        userRepository.deleteAll();

        userRepository.save(User.builder()
                .email("demo@optimiam.fr")
                .username("Utilisateur Démo")
                .password(passwordEncoder.encode("demo123"))
                .role(Role.ROLE_USER)
                .maxPrepTimeMinutes(30)
                .build());
    }

    @Test
    @DisplayName("POST /api/v1/auth/login - Doit authentifier l'utilisateur et retourner un token JWT")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest req = LoginRequest.builder()
                .email("demo@optimiam.fr")
                .password("demo123")
                .build();

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("demo@optimiam.fr")))
                .andExpect(jsonPath("$.user.username", is("Utilisateur Démo")));
    }

    @Test
    @DisplayName("POST /api/v1/auth/register - Doit créer un nouveau compte")
    void shouldRegisterNewUser() throws Exception {
        RegisterRequest req = RegisterRequest.builder()
                .username("Bob")
                .email("bob@optimiam.fr")
                .password("password123")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token", notNullValue()))
                .andExpect(jsonPath("$.user.email", is("bob@optimiam.fr")));
    }

    @Test
    @DisplayName("PUT /api/v1/auth/preferences - Doit mettre à jour les préférences")
    void shouldUpdatePreferences() throws Exception {
        UpdatePreferencesRequest req = UpdatePreferencesRequest.builder()
                .maxPrepTimeMinutes(20)
                .vegetarian(true)
                .glutenFree(false)
                .build();

        mockMvc.perform(put("/api/v1/auth/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.maxPrepTimeMinutes", is(20)))
                .andExpect(jsonPath("$.vegetarian", is(true)));
    }
}
