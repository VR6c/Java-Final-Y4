package com.example.demo.service.auth;

import com.example.demo.config.CacheConfig;
import com.example.demo.dto.request.auth.UpdateUserRequest;
import com.example.demo.dto.response.auth.UserResponse;
import com.example.demo.entity.auth.User;
import com.example.demo.repository.auth.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AppUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = CacheConfig.CACHE_USERS, key = "#currentUser.id")
    public UserResponse getCurrentUser(User currentUser) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + currentUser.getId()));
        return UserResponse.fromEntity(user);
    }

    @Caching(evict = {
            @CacheEvict(value = CacheConfig.CACHE_USERS, key = "#currentUser.id"),
            @CacheEvict(value = CacheConfig.CACHE_USER_DETAILS, key = "#currentUser.email")
    })
    @Transactional
    public UserResponse updateCurrentUser(User currentUser, UpdateUserRequest request) {
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + currentUser.getId()));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName().trim());
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }

    public void logout() {
        SecurityContextHolder.clearContext();
    }
}
