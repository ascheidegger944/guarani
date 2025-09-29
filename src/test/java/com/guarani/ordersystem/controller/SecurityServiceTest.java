package com.guarani.ordersystem.controller;

import com.guarani.ordersystem.util.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private SecurityService securityService;

    @Test
    void isOwner_ShouldReturnTrue_WhenCurrentUserIsOwner() {
        // Arrange
        when(securityUtils.getCurrentUsername()).thenReturn("user@email.com");
        // Aqui você implementaria a lógica real de verificação de ownership

        // Act
        boolean result = securityService.isOwner(1L);

        // Assert
        assertTrue(result); // Placeholder - implementar lógica real
    }

    @Test
    void isOwner_ShouldReturnFalse_WhenCurrentUserIsNotOwner() {
        // Arrange
        when(securityUtils.getCurrentUsername()).thenReturn("other@email.com");
        // Aqui você implementaria a lógica real de verificação de ownership

        // Act
        boolean result = securityService.isOwner(1L);

        // Assert
        assertFalse(result); // Placeholder - implementar lógica real
    }
}