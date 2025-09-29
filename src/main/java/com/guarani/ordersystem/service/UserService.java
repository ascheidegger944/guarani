package com.guarani.ordersystem.service;

import com.guarani.ordersystem.dto.UserResponse;
import com.guarani.ordersystem.entity.User;
import com.guarani.ordersystem.entity.enums.Role;
import com.guarani.ordersystem.exception.ResourceNotFoundException;
import com.guarani.ordersystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "users")
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(key = "#id")
    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        log.info("Buscando usuário por ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse findByEmail(String email) {
        log.info("Buscando usuário por email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "email", email));
        return mapToUserResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(Pageable pageable) {
        log.info("Buscando todos os usuários paginados");
        return userRepository.findAll(pageable)
                .map(this::mapToUserResponse);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> findByRole(Role role) {
        log.info("Buscando usuários por role: {}", role);
        return userRepository.findByRolesContaining(role)
                .stream()
                .map(this::mapToUserResponse)
                .toList();
    }

    @CacheEvict(key = "#id")
    @Transactional
    public UserResponse updateUserRoles(Long id, List<Role> roles) {
        log.info("Atualizando roles do usuário ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        user.getRoles().clear();
        roles.forEach(user::addRole);

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @CacheEvict(key = "#id")
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deletando usuário ID: {}", id);
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("Usuário", "id", id);
        }
        userRepository.deleteById(id);
    }

    @CacheEvict(allEntries = true)
    @Transactional
    public UserResponse updateProfile(Long id, String name, String email) {
        log.info("Atualizando perfil do usuário ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário", "id", id));

        if (email != null && !email.equals(user.getEmail())) {
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("Email já está em uso");
            }
            user.setEmail(email);
        }

        if (name != null) {
            user.setName(name);
        }

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .roles(user.getRoles())
                .createdAt(user.getCreatedAt())
                .build();
    }
}