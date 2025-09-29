package com.guarani.ordersystem.dto;

import com.guarani.ordersystem.entity.enums.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRolesUpdateRequest {

    @NotNull(message = "Roles não pode ser nulo")
    private Set<Role> roles;
}