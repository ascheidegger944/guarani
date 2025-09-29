package com.guarani.ordersystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarani.ordersystem.dto.AuthenticationRequest;
import com.guarani.ordersystem.dto.AuthenticationResponse;
import com.guarani.ordersystem.dto.RegisterRequest;
import com.guarani.ordersystem.entity.User;
import com.guarani.ordersystem.entity.enums.Role;
import com.guarani.ordersystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void register_ShouldCreateUserAndReturnToken() throws Exception {
        // Arrange
        RegisterRequest request = RegisterRequest.builder()
                .name("John Doe")
                .email("john@email.com")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuário registrado com sucesso"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.email").value("john@email.com"))
                .andExpect(jsonPath("$.data.name").value("John Doe"));

        // Verify user was saved in database
        User savedUser = userRepository.findByEmail("john@email.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("John Doe", savedUser.getName());
        assertTrue(savedUser.getRoles().contains(Role.CLIENT));
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void register_ShouldReturnError_WhenEmailAlreadyExists() throws Exception {
        // Arrange
        User existingUser = User.builder()
                .name("Existing User")
                .email("existing@email.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.CLIENT))
                .build();
        userRepository.save(existingUser);

        RegisterRequest request = RegisterRequest.builder()
                .name("New User")
                .email("existing@email.com") // Same email
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email já está em uso"));
    }

    @Test
    void login_ShouldReturnToken_WhenValidCredentials() throws Exception {
        // Arrange
        User user = User.builder()
                .name("Test User")
                .email("test@email.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.CLIENT))
                .build();
        userRepository.save(user);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@email.com")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login realizado com sucesso"))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.email").value("test@email.com"))
                .andExpect(jsonPath("$.data.name").value("Test User"));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenInvalidCredentials() throws Exception {
        // Arrange
        User user = User.builder()
                .name("Test User")
                .email("test@email.com")
                .password(passwordEncoder.encode("password123"))
                .roles(Set.of(Role.CLIENT))
                .build();
        userRepository.save(user);

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("test@email.com")
                .password("wrongpassword") // Wrong password
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Credenciais inválidas"));
    }

    @Test
    void login_ShouldReturnUnauthorized_WhenUserNotFound() throws Exception {
        // Arrange
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("nonexistent@email.com")
                .password("password123")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void registerAndLogin_ShouldWork_WhenValidFlow() throws Exception {
        // Arrange - Register
        RegisterRequest registerRequest = RegisterRequest.builder()
                .name("Integration User")
                .email("integration@email.com")
                .password("password123")
                .build();

        // Act & Assert - Register
        var registerResult = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andReturn();

        // Extract token from registration
        String registerResponse = registerResult.getResponse().getContentAsString();
        AuthenticationResponse authResponse = objectMapper.readValue(
                registerResponse,
                com.guarani.ordersystem.dto.ApiResponse.class
        ).getData();

        assertNotNull(authResponse.getToken());

        // Arrange - Login with same credentials
        AuthenticationRequest loginRequest = AuthenticationRequest.builder()
                .email("integration@email.com")
                .password("password123")
                .build();

        // Act & Assert - Login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").exists())
                .andExpect(jsonPath("$.data.email").value("integration@email.com"));
    }
}