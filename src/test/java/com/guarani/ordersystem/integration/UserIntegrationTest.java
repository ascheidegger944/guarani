package com.guarani.ordersystem.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guarani.ordersystem.dto.UserResponse;
import com.guarani.ordersystem.dto.UserRolesUpdateRequest;
import com.guarani.ordersystem.dto.UserUpdateRequest;
import com.guarani.ordersystem.entity.User;
import com.guarani.ordersystem.entity.enums.Role;
import com.guarani.ordersystem.repository.UserRepository;
import com.guarani.ordersystem.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    private String adminToken;
    private String clientToken;
    private User adminUser;
    private User clientUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        // Create admin user
        adminUser = User.builder()
                .name("Admin User")
                .email("admin@email.com")
                .password("password")
                .roles(Set.of(Role.ADMIN))
                .build();
        userRepository.save(adminUser);
        adminToken = jwtService.generateToken(adminUser);

        // Create client user
        clientUser = User.builder()
                .name("Client User")
                .email("client@email.com")
                .password("password")
                .roles(Set.of(Role.CLIENT))
                .build();
        userRepository.save(clientUser);
        clientToken = jwtService.generateToken(clientUser);
    }

    @Test
    void getAllUsers_ShouldReturnUsers_WhenAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content.length()").value(2)) // Both users
                .andExpect(jsonPath("$.data.content[0].name").exists())
                .andExpect(jsonPath("$.data.content[1].name").exists());
    }

    @Test
    void getAllUsers_ShouldReturnForbidden_WhenClient() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users")
                        .header("Authorization", "Bearer " + clientToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_ShouldReturnUser_WhenAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/{id}", clientUser.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(clientUser.getId()))
                .andExpect(jsonPath("$.data.name").value("Client User"))
                .andExpect(jsonPath("$.data.email").value("client@email.com"));
    }

    @Test
    void updateUserRoles_ShouldUpdateRoles_WhenAdmin() throws Exception {
        // Arrange
        UserRolesUpdateRequest request = UserRolesUpdateRequest.builder()
                .roles(Set.of(Role.CLIENT, Role.OPERATOR))
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}/roles", clientUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Roles atualizadas com sucesso"))
                .andExpect(jsonPath("$.data.roles.length()").value(2))
                .andExpect(jsonPath("$.data.roles[0]").value("CLIENT"))
                .andExpect(jsonPath("$.data.roles[1]").value("OPERATOR"));

        // Verify roles were updated in database
        User updatedUser = userRepository.findById(clientUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals(2, updatedUser.getRoles().size());
        assertTrue(updatedUser.getRoles().contains(Role.CLIENT));
        assertTrue(updatedUser.getRoles().contains(Role.OPERATOR));
    }

    @Test
    void updateProfile_ShouldUpdateProfile_WhenOwner() throws Exception {
        // Arrange
        UserUpdateRequest request = UserUpdateRequest.builder()
                .name("Updated Client Name")
                .email("updated.client@email.com")
                .build();

        // Act & Assert
        mockMvc.perform(put("/api/users/{id}/profile", clientUser.getId())
                        .header("Authorization", "Bearer " + clientToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Perfil atualizado com sucesso"))
                .andExpect(jsonPath("$.data.name").value("Updated Client Name"))
                .andExpect(jsonPath("$.data.email").value("updated.client@email.com"));

        // Verify profile was updated in database
        User updatedUser = userRepository.findById(clientUser.getId()).orElse(null);
        assertNotNull(updatedUser);
        assertEquals("Updated Client Name", updatedUser.getName());
        assertEquals("updated.client@email.com", updatedUser.getEmail());
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/users/{id}", clientUser.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Usuário excluído com sucesso"));

        // Verify user was deleted from database
        User deletedUser = userRepository.findById(clientUser.getId()).orElse(null);
        assertNull(deletedUser);
    }

    @Test
    void getUsersByRole_ShouldReturnUsers_WhenAdmin() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/users/role/CLIENT")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].email").value("client@email.com"));
    }
}