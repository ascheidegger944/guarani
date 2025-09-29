package com.guarani.ordersystem.controller;

import com.guarani.ordersystem.util.SecurityUtils;
import org.springframework.stereotype.Service;

@Service("securityService")
public class SecurityService {

    public boolean isOwner(Long userId) {
        // Implementação para verificar se o usuário atual é o proprietário do recurso
        String currentUser = SecurityUtils.getCurrentUsername();
        // Lógica para verificar ownership baseada no contexto
        return true; // Placeholder
    }
}