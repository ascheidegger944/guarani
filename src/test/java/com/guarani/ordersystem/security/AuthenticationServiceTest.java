package com.guarani.ordersystem.security;

import com.guarani.ordersystem.dto.AuthenticationRequest;
import com.guarani.ordersystem.dto.AuthenticationResponse;
import com.guarani.ordersystem.dto.RegisterRequest;
import com.guarani.ordersystem.entity.User;
import com.guarani.ordersystem.entity.enums.Role;
import com.guarani.ordersystem.exception.AuthenticationException;
import com.guarani.ordersystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authRequest;
    private User existingUser;
    private User newUser;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .name("John Doe")
                .email("john@email.com")
                .password("password123")
                .build();

        authRequest = AuthenticationRequest.builder()
                .email("john@email.com")
                .password("password123")
                .build();

        existingUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@email.com")
                .password("encodedPassword")
                .roles(Set.of(Role.CLIENT))
                .build();

        newUser = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@email.com")
                .password("encodedPassword")
                .roles(Set.of(Role.CLIENT))
                .build();
    }

    @Test
    void register_ShouldRegisterUser_WhenEmailNotExists() {
        // Arrange
        when(userRepository.existsByEmail("john@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("john@email.com", response.getEmail());
        assertEquals("John Doe", response.getName());

        verify(userRepository, times(1)).existsByEmail("john@email.com");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(jwtService, times(1)).generateToken(any(User.class));
    }

    @Test
    void register_ShouldThrowException_WhenEmailAlreadyExists() {
        // Arrange
        when(userRepository.existsByEmail("john@email.com")).thenReturn(true);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.register(registerRequest));

        assertEquals("Email já está em uso", exception.getMessage());
        assertEquals("EMAIL_ALREADY_EXISTS", exception.getErrorCode());

        verify(userRepository, times(1)).existsByEmail("john@email.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void register_ShouldAssignClientRole_ToNewUser() {
        // Arrange
        when(userRepository.existsByEmail("john@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertTrue(user.getRoles().contains(Role.CLIENT));
            assertEquals(1, user.getRoles().size());
            return newUser;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository).save(argThat(user ->
                user.getRoles().contains(Role.CLIENT) && user.getRoles().size() == 1
        ));
    }

    @Test
    void register_ShouldEncodePassword() {
        // Arrange
        when(userRepository.existsByEmail("john@email.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            assertEquals("encodedPassword123", user.getPassword());
            return newUser;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertNotNull(response);
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository).save(argThat(user ->
                "encodedPassword123".equals(user.getPassword())
        ));
    }

    @Test
    void authenticate_ShouldReturnToken_WhenValidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(existingUser)).thenReturn("jwt-token");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("john@email.com", response.getEmail());
        assertEquals("John Doe", response.getName());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("john@email.com");
        verify(jwtService, times(1)).generateToken(existingUser);
    }

    @Test
    void authenticate_ShouldThrowException_WhenInvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate(authRequest));

        assertEquals("Credenciais inválidas", exception.getMessage());
        assertEquals("INVALID_CREDENTIALS", exception.getErrorCode());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void authenticate_ShouldThrowException_WhenUserNotFound() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.empty());

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate(authRequest));

        assertEquals("Usuário não encontrado", exception.getMessage());
        assertEquals("USER_NOT_FOUND", exception.getErrorCode());

        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, times(1)).findByEmail("john@email.com");
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void authenticate_ShouldUseAuthenticationManager_WithCorrectCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenAnswer(invocation -> {
                    UsernamePasswordAuthenticationToken token = invocation.getArgument(0);
                    assertEquals("john@email.com", token.getPrincipal());
                    assertEquals("password123", token.getCredentials());
                    return mock(org.springframework.security.core.Authentication.class);
                });
        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(existingUser)).thenReturn("jwt-token");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        verify(authenticationManager).authenticate(argThat(token ->
                token instanceof UsernamePasswordAuthenticationToken &&
                        "john@email.com".equals(token.getPrincipal()) &&
                        "password123".equals(token.getCredentials())
        ));
    }

    @Test
    void register_ShouldHandleSpecialCharactersInEmail() {
        // Arrange
        RegisterRequest specialEmailRequest = RegisterRequest.builder()
                .name("Special User")
                .email("user+tag@domain.com")
                .password("password123")
                .build();

        User specialUser = User.builder()
                .id(2L)
                .name("Special User")
                .email("user+tag@domain.com")
                .password("encodedPassword")
                .roles(Set.of(Role.CLIENT))
                .build();

        when(userRepository.existsByEmail("user+tag@domain.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(specialUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthenticationResponse response = authenticationService.register(specialEmailRequest);

        // Assert
        assertNotNull(response);
        assertEquals("user+tag@domain.com", response.getEmail());
        verify(userRepository, times(1)).existsByEmail("user+tag@domain.com");
    }

    @Test
    void authenticate_ShouldWorkWithDifferentEmailCases() {
        // Arrange
        AuthenticationRequest upperCaseRequest = AuthenticationRequest.builder()
                .email("JOHN@EMAIL.COM")
                .password("password123")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userRepository.findByEmail("JOHN@EMAIL.COM")).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(existingUser)).thenReturn("jwt-token");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(upperCaseRequest);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).findByEmail("JOHN@EMAIL.COM");
    }

    @Test
    void register_ShouldHandleLongNamesAndEmails() {
        // Arrange
        RegisterRequest longDataRequest = RegisterRequest.builder()
                .name("A".repeat(255)) // Maximum length
                .email("a".repeat(240) + "@email.com") // Long email
                .password("password123")
                .build();

        User longDataUser = User.builder()
                .id(3L)
                .name("A".repeat(255))
                .email("a".repeat(240) + "@email.com")
                .password("encodedPassword")
                .roles(Set.of(Role.CLIENT))
                .build();

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(longDataUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Act
        AuthenticationResponse response = authenticationService.register(longDataRequest);

        // Assert
        assertNotNull(response);
        assertEquals("A".repeat(255), response.getName());
        verify(userRepository, times(1)).save(argThat(user ->
                user.getName().length() == 255 &&
                        user.getEmail().length() > 240
        ));
    }

    @Test
    void authenticate_ShouldHandleEmptyPassword() {
        // Arrange
        AuthenticationRequest emptyPasswordRequest = AuthenticationRequest.builder()
                .email("john@email.com")
                .password("")
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        assertThrows(AuthenticationException.class,
                () -> authenticationService.authenticate(emptyPasswordRequest));

        verify(authenticationManager, times(1)).authenticate(argThat(token ->
                "".equals(token.getCredentials())
        ));
    }

    @Test
    void register_ShouldNotAllowDuplicateRegistration() {
        // Arrange
        when(userRepository.existsByEmail("john@email.com")).thenReturn(true);

        // Act & Assert - First registration should work, second should fail
        assertThrows(AuthenticationException.class,
                () -> authenticationService.register(registerRequest));

        verify(userRepository, times(1)).existsByEmail("john@email.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_ShouldReturnUserDetailsInResponse() {
        // Arrange
        User userWithDetails = User.builder()
                .id(1L)
                .name("John Doe")
                .email("john@email.com")
                .password("encodedPassword")
                .roles(Set.of(Role.CLIENT, Role.OPERATOR))
                .build();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(mock(org.springframework.security.core.Authentication.class));
        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(userWithDetails));
        when(jwtService.generateToken(userWithDetails)).thenReturn("jwt-token");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("john@email.com", response.getEmail());
        assertEquals("John Doe", response.getName());
        // Note: Roles are not included in AuthenticationResponse by design
    }
}