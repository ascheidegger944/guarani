package com.guarani.ordersystem.controller;

import com.guarani.ordersystem.dto.*;
import com.guarani.ordersystem.entity.enums.Role;
import com.guarani.ordersystem.service.UserService;
import com.guarani.ordersystem.util.Constants;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Usuários", description = "APIs para gerenciamento de usuários")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(summary = "Obter perfil do usuário atual", description = "Retorna os dados do usuário autenticado")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        // Implementação para obter usuário atual do contexto de segurança
        return ResponseEntity.ok(ApiResponse.success("Perfil obtido com sucesso", null));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários", description = "Retorna lista paginada de usuários (apenas ADMIN)")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_NUMBER) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction
    ) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<UserResponse> users = userService.findAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(users)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    @Operation(summary = "Obter usuário por ID", description = "Retorna usuário específico por ID")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success("Usuário encontrado", user));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obter usuário por email", description = "Retorna usuário específico por email (apenas ADMIN)")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@PathVariable String email) {
        UserResponse user = userService.findByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("Usuário encontrado", user));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar usuários por role", description = "Retorna usuários com role específica (apenas ADMIN)")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable Role role) {
        List<UserResponse> users = userService.findByRole(role);
        return ResponseEntity.ok(ApiResponse.success("Usuários encontrados", users));
    }

    @PutMapping("/{id}/profile")
    @PreAuthorize("hasRole('ADMIN') or @securityService.isOwner(#id)")
    @Operation(summary = "Atualizar perfil", description = "Atualiza dados do perfil do usuário")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request
    ) {
        UserResponse user = userService.updateProfile(id, request.getName(), request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Perfil atualizado com sucesso", user));
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Atualizar roles", description = "Atualiza roles do usuário (apenas ADMIN)")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRoles(
            @PathVariable Long id,
            @Valid @RequestBody UserRolesUpdateRequest request
    ) {
        UserResponse user = userService.updateUserRoles(id, request.getRoles());
        return ResponseEntity.ok(ApiResponse.success("Roles atualizadas com sucesso", user));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Excluir usuário", description = "Exclui usuário do sistema (apenas ADMIN)")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("Usuário excluído com sucesso"));
    }
}