package com.generic.rest.main.service;

import com.generic.rest.main.dto.auth.AuthRequest;
import com.generic.rest.main.dto.auth.AuthResponse;
import com.generic.rest.main.model.User;
import com.generic.rest.main.model.enums.Role;
import com.generic.rest.main.repository.UserRepository;
import com.generic.rest.main.security.JwtService;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final GoogleOAuthService googleOAuthService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, GoogleOAuthService googleOAuthService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.googleOAuthService = googleOAuthService;
    }

    @Transactional
    public AuthResponse signup(AuthRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }
        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);
        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        List<String> menuItems = getMenuItems(user);
        return new AuthResponse(token, menuItems, user.getRole().name());
    }

    public AuthResponse login(AuthRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        String token = jwtService.generateToken(user.getEmail());
        List<String> menuItems = getMenuItems(user);
        return new AuthResponse(token, menuItems, user.getRole().name());
    }

    public AuthResponse getRole(User user) {
        return new AuthResponse(null, null, user.getRole().name());
    }

    public AuthResponse getAccountMenu(User user) {
        List<String> menuItems = getMenuItems(user);
        return new AuthResponse(null, menuItems, null);
    }

    @Transactional
    public AuthResponse googleLogin(String googleToken) {
        // Validate Google token and extract user information
        GoogleOAuthService.GoogleUserInfo googleUserInfo;
        try {
            googleUserInfo = googleOAuthService.validateToken(googleToken);
        } catch (RuntimeException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid Google OAuth token: " + e.getMessage());
        }

        // Check if user already exists
        User user = userRepository.findByEmail(googleUserInfo.email())
                .orElseGet(() -> {
                    // Create new user if doesn't exist
                    User newUser = new User();
                    newUser.setEmail(googleUserInfo.email());
                    newUser.setFirstName(googleUserInfo.givenName());
                    newUser.setLastName(googleUserInfo.familyName());
                    // Set a random password hash (user won't be able to login with password)
                    newUser.setPasswordHash(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
                    newUser.setRole(Role.USER);
                    return userRepository.save(newUser);
                });

        // Generate JWT token for our application
        String token = jwtService.generateToken(user.getEmail());
        List<String> menuItems = getMenuItems(user);
        return new AuthResponse(token, menuItems, user.getRole().name());
    }

    private List<String> getMenuItems(User user) {
        if (user.getRole() == Role.ADMIN) {
            return Arrays.asList("ORDER", "ADDRESS", "PRODUCTS", "BILLING", "CONFIGURATION");
        } else {
            return Arrays.asList("ORDER", "ADDRESS");
        }
    }
}


