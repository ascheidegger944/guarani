package com.guarani.ordersystem.service;

import com.guarani.ordersystem.dto.UserResponse;
import com.guarani.ordersystem.entity.User;
import com.guarani.ordersystem.entity.enums.Role;
import com.guarani.ordersystem.exception.ResourceNotFoundException;
import com.guarani.ordersystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private User adminUser;
    private User operatorUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@email.com")
                .password("encodedPassword")
                .roles(Set.of(Role.CLIENT))
                .createdAt(LocalDateTime.now())
                .build();

        adminUser = User.builder()
                .id(2L)
                .name("Admin User")
                .email("admin@email.com")
                .password("encodedPassword")
                .roles(Set.of(Role.ADMIN))
                .createdAt(LocalDateTime.now())
                .build();

        operatorUser = User.builder()
                .id(3L)
                .name("Operator User")
                .email("operator@email.com")
                .password("encodedPassword")
                .roles(Set.of(Role.OPERATOR))
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Test
    void findById_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.findById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertEquals("john@email.com", result.getEmail());
        assertTrue(result.getRoles().contains(Role.CLIENT));
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    void findById_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.findById(999L));
        verify(userRepository, times(1)).findById(999L);
    }

    @Test
    void findByEmail_ShouldReturnUser_WhenUserExists() {
        // Arrange
        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(testUser));

        // Act
        UserResponse result = userService.findByEmail("john@email.com");

        // Assert
        assertNotNull(result);
        assertEquals("john@email.com", result.getEmail());
        assertEquals("John Doe", result.getName());
        verify(userRepository, times(1)).findByEmail("john@email.com");
    }

    @Test
    void findByEmail_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@email.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.findByEmail("nonexistent@email.com"));
        verify(userRepository, times(1)).findByEmail("nonexistent@email.com");
    }

    @Test
    void findAll_ShouldReturnPageOfUsers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        List<User> users = List.of(testUser, adminUser, operatorUser);
        Page<User> userPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(pageable)).thenReturn(userPage);

        // Act
        Page<UserResponse> result = userService.findAll(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.getTotalElements());
        assertEquals(3, result.getContent().size());
        assertEquals("John Doe", result.getContent().get(0).getName());
        assertEquals("Admin User", result.getContent().get(1).getName());
        verify(userRepository, times(1)).findAll(pageable);
    }

    @Test
    void findByRole_ShouldReturnUsersWithRole() {
        // Arrange
        List<User> adminUsers = List.of(adminUser);
        when(userRepository.findByRolesContaining(Role.ADMIN)).thenReturn(adminUsers);

        // Act
        List<UserResponse> result = userService.findByRole(Role.ADMIN);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Admin User", result.get(0).getName());
        assertTrue(result.get(0).getRoles().contains(Role.ADMIN));
        verify(userRepository, times(1)).findByRolesContaining(Role.ADMIN);
    }

    @Test
    void findByRole_ShouldReturnEmptyList_WhenNoUsersWithRole() {
        // Arrange
        when(userRepository.findByRolesContaining(Role.ADMIN)).thenReturn(List.of());

        // Act
        List<UserResponse> result = userService.findByRole(Role.ADMIN);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByRolesContaining(Role.ADMIN);
    }

    @Test
    void updateUserRoles_ShouldUpdateRoles_WhenUserExists() {
        // Arrange
        Set<Role> newRoles = Set.of(Role.CLIENT, Role.OPERATOR);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = userService.updateUserRoles(1L, List.copyOf(newRoles));

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.CLIENT));
        assertTrue(result.getRoles().contains(Role.OPERATOR));
        assertFalse(result.getRoles().contains(Role.ADMIN));

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserRoles_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        Set<Role> newRoles = Set.of(Role.CLIENT, Role.OPERATOR);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUserRoles(999L, List.copyOf(newRoles)));
        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void deleteUser_ShouldDeleteUser_WhenUserExists() {
        // Arrange
        when(userRepository.existsById(1L)).thenReturn(true);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).existsById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUser(999L));
        verify(userRepository, times(1)).existsById(999L);
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void updateProfile_ShouldUpdateNameAndEmail_WhenValidData() {
        // Arrange
        String newName = "John Updated";
        String newEmail = "john.updated@email.com";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = userService.updateProfile(1L, newName, newEmail);

        // Assert
        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(newEmail, result.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail(newEmail);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldUpdateOnlyName_WhenEmailIsNull() {
        // Arrange
        String newName = "John Updated";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = userService.updateProfile(1L, newName, null);

        // Assert
        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals("john@email.com", result.getEmail()); // Email remains unchanged

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldUpdateOnlyEmail_WhenNameIsNull() {
        // Arrange
        String newEmail = "john.updated@email.com";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = userService.updateProfile(1L, null, newEmail);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName()); // Name remains unchanged
        assertEquals(newEmail, result.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail(newEmail);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        String newEmail = "existing@email.com";

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByEmail(newEmail)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(1L, "New Name", newEmail));

        assertEquals("Email já está em uso", exception.getMessage());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, times(1)).existsByEmail(newEmail);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldNotCheckEmail_WhenEmailNotChanged() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act - Same email as current user
        UserResponse result = userService.updateProfile(1L, "New Name", "john@email.com");

        // Assert
        assertNotNull(result);
        assertEquals("New Name", result.getName());
        assertEquals("john@email.com", result.getEmail());

        verify(userRepository, times(1)).findById(1L);
        verify(userRepository, never()).existsByEmail(anyString()); // Should not check if email is the same
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateProfile_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateProfile(999L, "New Name", "new@email.com"));

        verify(userRepository, times(1)).findById(999L);
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void mapToUserResponse_ShouldMapAllFieldsCorrectly() {
        // Arrange
        User user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@email.com")
                .roles(Set.of(Role.CLIENT, Role.OPERATOR))
                .createdAt(LocalDateTime.of(2024, 1, 1, 10, 0))
                .build();

        // Act
        UserResponse result = userService.findById(1L); // This will use the mapping method internally

        // This test is more about verifying the mapping logic in the service
        // Since we're mocking the repository, we need to test the mapping separately
        // In a real scenario, you might want to extract the mapping to a separate method and test it

        // For now, let's verify that when we call the service method, it returns the expected structure
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponse response = userService.findById(1L);

        // Assert
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getName(), response.getName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getRoles(), response.getRoles());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void updateUserRoles_ShouldClearExistingRoles() {
        // Arrange
        // User starts with CLIENT role
        User userWithRoles = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@email.com")
                .roles(Set.of(Role.CLIENT, Role.OPERATOR)) // Multiple existing roles
                .build();

        Set<Role> newRoles = Set.of(Role.ADMIN); // Only one new role

        when(userRepository.findById(1L)).thenReturn(Optional.of(userWithRoles));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = userService.updateUserRoles(1L, List.copyOf(newRoles));

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getRoles().size());
        assertTrue(result.getRoles().contains(Role.ADMIN));
        assertFalse(result.getRoles().contains(Role.CLIENT));
        assertFalse(result.getRoles().contains(Role.OPERATOR));

        // Verify that save was called with user having only the new roles
        verify(userRepository).save(argThat(savedUser ->
                savedUser.getRoles().size() == 1 && savedUser.getRoles().contains(Role.ADMIN)
        ));
    }

    @Test
    void updateUserRoles_ShouldHandleEmptyRolesList() {
        // Arrange
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        UserResponse result = userService.updateUserRoles(1L, List.of());

        // Assert
        assertNotNull(result);
        assertTrue(result.getRoles().isEmpty());

        verify(userRepository).save(argThat(savedUser ->
                savedUser.getRoles().isEmpty()
        ));
    }
}