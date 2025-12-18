package com.boombet.auth_service.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boombet.auth_service.dto.LoginRequest;
import com.boombet.auth_service.dto.LoginResponse;
import com.boombet.auth_service.dto.RegisterRequest;
import com.boombet.auth_service.service.AuthService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration attempt for email: {}", request.email());
        try {
            authService.register(request);
            log.info("User registered successfully: {}", request.email());
            return ResponseEntity.ok("User registered successfully!");
        } catch (IllegalStateException e) {
            log.warn("Registration failed for {}: {}", request.email(), e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Login attempt for email: {}", request.email());
        try {
            LoginResponse response = authService.login(request);
            log.info("User logged in successfully: {}", request.email());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.warn("Login failed for {}: {}", request.email(), e.getMessage());
            return ResponseEntity.status(401).build();
        }
    }
}
