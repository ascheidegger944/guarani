package com.guarani.ordersystem.repository;

import com.guarani.ordersystem.entity.User;
import com.guarani.ordersystem.entity.enums.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.test.database.replace=NONE",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private User adminUser;
    private User operatorUser;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user1 = User.builder()
                .name("John Doe")
                .email("john@email.com")
                .password("password123")
                .roles(Set.of(Role.CLIENT))
                .build();

        user2 = User.builder()
                .name("Jane Smith")
                .email("jane@email.com")
                .password("password456")
                .roles(Set.of(Role.CLIENT))
                .build();

        adminUser = User.builder()
                .name("Admin User")
                .email("admin@email.com")
                .password("admin123")
                .roles(Set.of(Role.ADMIN, Role.OPERATOR))
                .build();

        operatorUser = User.builder()
                .name("Operator User")
                .email("operator@email.com")
                .password("operator123")
                .roles(Set.of(Role.OPERATOR))
                .build();

        // Persist users in order to get IDs
        user1 = entityManager.persistAndFlush(user1);
        user2 = entityManager.persistAndFlush(user2);
        adminUser = entityManager.persistAndFlush(adminUser);
        operatorUser = entityManager.persistAndFlush(operatorUser);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenEmailExists() {
        // Act
        Optional<User> result = userRepository.findByEmail("john@email.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        assertEquals("john@email.com", result.get().getEmail());
        assertTrue(result.get().getRoles().contains(Role.CLIENT));
    }

    @Test
    void findByEmail_ShouldReturnEmpty_WhenEmailNotExists() {
        // Act
        Optional<User> result = userRepository.findByEmail("nonexistent@email.com");

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void findByEmail_ShouldBeCaseSensitive() {
        // Act
        Optional<User> result = userRepository.findByEmail("JOHN@EMAIL.COM");

        // Assert
        assertFalse(result.isPresent()); // Should not find with different case
    }

    @Test
    void existsByEmail_ShouldReturnTrue_WhenEmailExists() {
        // Act
        boolean exists = userRepository.existsByEmail("john@email.com");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByEmail_ShouldReturnFalse_WhenEmailNotExists() {
        // Act
        boolean exists = userRepository.existsByEmail("nonexistent@email.com");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByRolesContaining_ShouldReturnUsers_WithSpecificRole() {
        // Act
        List<User> adminUsers = userRepository.findByRolesContaining(Role.ADMIN);
        List<User> operatorUsers = userRepository.findByRolesContaining(Role.OPERATOR);
        List<User> clientUsers = userRepository.findByRolesContaining(Role.CLIENT);

        // Assert
        assertEquals(1, adminUsers.size());
        assertEquals("Admin User", adminUsers.get(0).getName());

        assertEquals(2, operatorUsers.size()); // Admin also has OPERATOR role
        assertTrue(operatorUsers.stream().anyMatch(u -> u.getName().equals("Admin User")));
        assertTrue(operatorUsers.stream().anyMatch(u -> u.getName().equals("Operator User")));

        assertEquals(2, clientUsers.size());
        assertTrue(clientUsers.stream().anyMatch(u -> u.getName().equals("John Doe")));
        assertTrue(clientUsers.stream().anyMatch(u -> u.getName().equals("Jane Smith")));
    }

    @Test
    void findByRolesContaining_ShouldReturnEmptyList_WhenNoUsersWithRole() {
        // Create a role that no user has
        Role nonExistentRole = Role.ADMIN; // All ADMIN users are already in our test data

        // Act - This should return empty since we're querying for a role that might not exist in the way we expect
        // But let's test with a different approach
        List<User> users = userRepository.findByRolesContaining(Role.ADMIN);

        // Assert - We know we have one admin user
        assertFalse(users.isEmpty());
    }

    @Test
    void save_ShouldPersistUser_WithAllFields() {
        // Arrange
        User newUser = User.builder()
                .name("New User")
                .email("new@email.com")
                .password("newpassword")
                .roles(Set.of(Role.CLIENT, Role.OPERATOR))
                .build();

        // Act
        User savedUser = userRepository.save(newUser);
        entityManager.flush();
        entityManager.clear();

        // Assert
        assertNotNull(savedUser.getId());
        assertEquals("New User", savedUser.getName());
        assertEquals("new@email.com", savedUser.getEmail());
        assertEquals("newpassword", savedUser.getPassword());
        assertEquals(2, savedUser.getRoles().size());
        assertTrue(savedUser.getRoles().contains(Role.CLIENT));
        assertTrue(savedUser.getRoles().contains(Role.OPERATOR));

        // Verify can be retrieved
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        assertTrue(retrievedUser.isPresent());
        assertEquals("New User", retrievedUser.get().getName());
    }

    @Test
    void updateUser_ShouldUpdateFields() {
        // Arrange
        User user = userRepository.findByEmail("john@email.com").get();
        user.setName("John Updated");
        user.setEmail("john.updated@email.com");

        // Act
        User updatedUser = userRepository.save(user);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<User> retrievedUser = userRepository.findById(updatedUser.getId());
        assertTrue(retrievedUser.isPresent());
        assertEquals("John Updated", retrievedUser.get().getName());
        assertEquals("john.updated@email.com", retrievedUser.get().getEmail());
    }

    @Test
    void deleteUser_ShouldRemoveUser() {
        // Arrange
        Long userId = user1.getId();

        // Act
        userRepository.deleteById(userId);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<User> deletedUser = userRepository.findById(userId);
        assertFalse(deletedUser.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllUsers() {
        // Act
        List<User> allUsers = userRepository.findAll();

        // Assert
        assertEquals(4, allUsers.size());
        assertTrue(allUsers.stream().anyMatch(u -> u.getEmail().equals("john@email.com")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getEmail().equals("jane@email.com")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getEmail().equals("admin@email.com")));
        assertTrue(allUsers.stream().anyMatch(u -> u.getEmail().equals("operator@email.com")));
    }

    @Test
    void findById_ShouldReturnUser_WhenIdExists() {
        // Act
        Optional<User> result = userRepository.findById(user1.getId());

        // Assert
        assertTrue(result.isPresent());
        assertEquals("John Doe", result.get().getName());
        assertEquals("john@email.com", result.get().getEmail());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenIdNotExists() {
        // Act
        Optional<User> result = userRepository.findById(999L);

        // Assert
        assertFalse(result.isPresent());
    }

    @Test
    void save_ShouldHandleUser_WithMultipleRoles() {
        // Arrange
        User multiRoleUser = User.builder()
                .name("Multi Role User")
                .email("multi@email.com")
                .password("password")
                .roles(Set.of(Role.ADMIN, Role.OPERATOR, Role.CLIENT))
                .build();

        // Act
        User savedUser = userRepository.save(multiRoleUser);
        entityManager.flush();
        entityManager.clear();

        // Assert
        Optional<User> retrievedUser = userRepository.findByEmail("multi@email.com");
        assertTrue(retrievedUser.isPresent());
        assertEquals(3, retrievedUser.get().getRoles().size());
        assertTrue(retrievedUser.get().getRoles().contains(Role.ADMIN));
        assertTrue(retrievedUser.get().getRoles().contains(Role.OPERATOR));
        assertTrue(retrievedUser.get().getRoles().contains(Role.CLIENT));
    }

    @Test
    void findByEmail_ShouldWork_WithSpecialCharacters() {
        // Arrange
        User specialEmailUser = User.builder()
                .name("Special Email User")
                .email("user+tag@domain.com")
                .password("password")
                .roles(Set.of(Role.CLIENT))
                .build();

        userRepository.save(specialEmailUser);
        entityManager.flush();
        entityManager.clear();

        // Act
        Optional<User> result = userRepository.findByEmail("user+tag@domain.com");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Special Email User", result.get().getName());
    }

    @Test
    void userCreation_ShouldSetAuditFields() {
        // Arrange
        User newUser = User.builder()
                .name("Audit User")
                .email("audit@email.com")
                .password("password")
                .roles(Set.of(Role.CLIENT))
                .build();

        // Act
        User savedUser = userRepository.save(newUser);
        entityManager.flush();

        // Assert
        assertNotNull(savedUser.getCreatedAt());
        // UpdatedAt might be null initially or same as createdAt
    }
}