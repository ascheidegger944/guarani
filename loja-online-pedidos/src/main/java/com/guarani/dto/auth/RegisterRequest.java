package com.guarani.dto.auth;

import com.guarani.model.UserRole;
import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String email;
    private String password;
    private UserRole role;
}