package com.guarani.ordersystem.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;
    private UserDetails userDetails;
    private UserDetails differentUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        // Set secret key and expiration using reflection
        ReflectionTestUtils.setField(jwtService, "secretKey",
                "mySecretKeyForJWTGenerationInGuaraniApp2024mySecretKeyForJWTGenerationInGuaraniApp2024");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L); // 24 hours

        userDetails = User.builder()
                .username("test@email.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        differentUser = User.builder()
                .username("other@email.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();
    }

    @Test
    void generateToken_ShouldReturnValidToken() {
        // Act
        String token = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.length() > 50); // Basic length check for JWT
    }

    @Test
    void generateToken_ShouldReturnDifferentTokens_ForDifferentCalls() {
        // Act
        String token1 = jwtService.generateToken(userDetails);
        String token2 = jwtService.generateToken(userDetails);

        // Assert
        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2); // Tokens should be different due to timestamp
    }

    @Test
    void generateToken_WithExtraClaims_ShouldIncludeClaims() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", 123L);
        extraClaims.put("role", "ADMIN");

        // Act
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Assert
        assertNotNull(token);

        // Extract claims and verify
        Claims claims = jwtService.extractAllClaims(token);
        assertEquals("test@email.com", claims.getSubject());
        assertEquals(123L, claims.get("userId"));
        assertEquals("ADMIN", claims.get("role"));
    }

    @Test
    void extractUsername_ShouldReturnUsername_FromValidToken() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@email.com", username);
    }

    @Test
    void extractClaim_ShouldReturnSpecificClaim() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        String subject = jwtService.extractClaim(token, Claims::getSubject);

        // Assert
        assertEquals("test@email.com", subject);
    }

    @Test
    void isTokenValid_ShouldReturnTrue_ForValidTokenAndUser() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForDifferentUser() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        boolean isValid = jwtService.isTokenValid(token, differentUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForExpiredToken() {
        // Arrange - Set very short expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 1L); // 1ms

        String token = jwtService.generateToken(userDetails);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Act
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Assert
        assertFalse(isValid);

        // Reset expiration for other tests
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
    }

    @Test
    void extractAllClaims_ShouldReturnAllClaims() {
        // Arrange
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("customClaim", "customValue");
        String token = jwtService.generateToken(extraClaims, userDetails);

        // Act
        Claims claims = jwtService.extractAllClaims(token);

        // Assert
        assertNotNull(claims);
        assertEquals("test@email.com", claims.getSubject());
        assertEquals("customValue", claims.get("customClaim"));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void extractExpiration_ShouldReturnExpirationDate() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // Act
        var expiration = jwtService.extractClaim(token, Claims::getExpiration);

        // Assert
        assertNotNull(expiration);
        assertTrue(expiration.after(new java.util.Date()));
    }

    @Test
    void isTokenExpired_ShouldReturnFalse_ForValidToken() {
        // Arrange
        String token = jwtService.generateToken(userDetails);

        // This is a private method, but we can test it through public methods
        // Act & Assert - Token should not be expired immediately after generation
        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid); // Implies token is not expired
    }

    @Test
    void generateToken_ShouldHandleSpecialCharactersInUsername() {
        // Arrange
        UserDetails specialUser = User.builder()
                .username("user+special@domain.com")
                .password("password")
                .authorities(Collections.emptyList())
                .build();

        // Act
        String token = jwtService.generateToken(specialUser);
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertNotNull(token);
        assertEquals("user+special@domain.com", extractedUsername);
    }

    @Test
    void generateToken_ShouldWorkWithDifferentUserDetailsImplementations() {
        // Arrange
        UserDetails customUserDetails = new org.springframework.security.core.userdetails.User(
                "custom@email.com",
                "password",
                Collections.emptyList()
        );

        // Act
        String token = jwtService.generateToken(customUserDetails);
        String extractedUsername = jwtService.extractUsername(token);

        // Assert
        assertNotNull(token);
        assertEquals("custom@email.com", extractedUsername);
    }

    @Test
    void extractUsername_ShouldThrowException_ForInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.here";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.extractUsername(invalidToken));
    }

    @Test
    void extractUsername_ShouldThrowException_ForMalformedToken() {
        // Arrange
        String malformedToken = "header.payload.signature";

        // Act & Assert
        assertThrows(Exception.class, () -> jwtService.extractUsername(malformedToken));
    }

    @Test
    void isTokenValid_ShouldReturnFalse_ForTamperedToken() {
        // Arrange
        String validToken = jwtService.generateToken(userDetails);
        String tamperedToken = validToken + "tampered";

        // Act
        boolean isValid = jwtService.isTokenValid(tamperedToken, userDetails);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void generateToken_ShouldIncludeIssuedAtAndExpiration() {
        // Act
        String token = jwtService.generateToken(userDetails);
        Claims claims = jwtService.extractAllClaims(token);

        // Assert
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
        assertTrue(claims.getExpiration().after(claims.getIssuedAt()));
    }

    @Test
    void tokenExpiration_ShouldBeConfigurable() {
        // Arrange - Set different expiration
        long shortExpiration = 1000L; // 1 second
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", shortExpiration);

        String token = jwtService.generateToken(userDetails);
        Claims claims = jwtService.extractAllClaims(token);

        // Calculate expected expiration (issuedAt + expiration)
        long expectedExpiration = claims.getIssuedAt().getTime() + shortExpiration;

        // Assert
        assertEquals(expectedExpiration, claims.getExpiration().getTime());

        // Reset expiration
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 86400000L);
    }
}