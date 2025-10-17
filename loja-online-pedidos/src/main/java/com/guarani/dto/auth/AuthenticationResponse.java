package com.guarani.dto.auth;

import lombok.Data;

@Data
public class AuthenticationResponse {
    private String token;
    private String username;
    private String role;

    public AuthenticationResponse(String token, String username, String role) {
        this.token = token;
        this.username = username;
        this.role = role;
    }
}