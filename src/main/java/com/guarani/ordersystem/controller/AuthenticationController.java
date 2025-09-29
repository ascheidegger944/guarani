package com.guarani.ordersystem.controller;

import com.guarani.ordersystem.dto.ApiResponse;
import com.guarani.ordersystem.dto.AuthenticationRequest;
import com.guarani.ordersystem.dto.AuthenticationResponse;
import com.guarani.ordersystem.dto.RegisterRequest;
import com.guarani.ordersystem.security.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticação", description = "APIs para autenticação e registro de usuários")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/register")
    @Operation(summary = "Registrar novo usuário", description = "Cria uma nova conta de usuário")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("Usuário registrado com sucesso", response));
    }

    @PostMapping("/login")
    @Operation(summary = "Login de usuário", description = "Autentica usuário e retorna token JWT")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(
            @Valid @RequestBody AuthenticationRequest request
    ) {
        AuthenticationResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success("Login realizado com sucesso", response));
    }
}