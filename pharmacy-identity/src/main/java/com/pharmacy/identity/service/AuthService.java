package com.pharmacy.identity.service;

import com.pharmacy.common.enums.Role;
import com.pharmacy.common.enums.UserStatus;
import com.pharmacy.common.exception.BadRequestException;
import com.pharmacy.common.exception.ResourceNotFoundException;
import com.pharmacy.common.exception.UnauthorizedException;
import com.pharmacy.common.events.UserLoggedInEvent;
import com.pharmacy.common.events.UserRegisteredEvent;
import com.pharmacy.common.util.JwtUtil;
import com.pharmacy.identity.dto.*;
import com.pharmacy.identity.entity.User;
import com.pharmacy.identity.event.IdentityEventPublisher;
import com.pharmacy.identity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final JwtBlacklistService jwtBlacklistService;
    private final IdentityEventPublisher identityEventPublisher;

    @Transactional
    public UserDTO signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .mobile(request.getMobile())
                .role(Role.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();

        User savedUser = userRepository.save(user);
        publishUserRegistered(savedUser, request.getName());
        return toUserDTO(savedUser);
    }

    private void publishUserLoggedIn(User user) {
        try {
            identityEventPublisher.publishUserLoggedIn(UserLoggedInEvent.builder()
                    .userId(user.getId())
                    .email(user.getEmail())
                    .firstName(firstNameFromFullName(user.getName()))
                    .build());
        } catch (Exception e) {
            log.warn("Could not publish UserLoggedInEvent userId={}: {}", user.getId(), e.getMessage());
        }
    }

    private void publishUserRegistered(User savedUser, String fullName) {
        try {
            identityEventPublisher.publishUserRegistered(UserRegisteredEvent.builder()
                    .userId(savedUser.getId())
                    .email(savedUser.getEmail())
                    .firstName(firstNameFromFullName(fullName))
                    .build());
        } catch (Exception e) {
            log.warn("Could not publish UserRegisteredEvent userId={}: {}", savedUser.getId(), e.getMessage());
        }
    }

    private static String firstNameFromFullName(String name) {
        if (name == null || name.isBlank()) {
            return "there";
        }
        int sp = name.indexOf(' ');
        return sp > 0 ? name.substring(0, sp) : name;
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UnauthorizedException("Account is not active");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail(), user.getRole().name());

        publishUserLoggedIn(user);

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole().name())
                .build();
    }

    public void logout(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        long expirationTime = jwtUtil.getExpirationTime(token);
        jwtBlacklistService.blacklistToken(token, expirationTime);
        log.info("User logged out, token blacklisted");
    }

    public boolean validateToken(String token) {
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        if (jwtBlacklistService.isTokenBlacklisted(token)) {
            return false;
        }
        
        return jwtUtil.validateToken(token);
    }

    public UserDTO getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return toUserDTO(user);
    }

    public UserDTO getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
        return toUserDTO(user);
    }

    private UserDTO toUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .build();
    }
}
