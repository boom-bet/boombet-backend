package com.boombet.auth_service.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.boombet.auth_service.dto.LoginRequest;
import com.boombet.auth_service.dto.LoginResponse;
import com.boombet.auth_service.dto.RegisterRequest;
import com.boombet.auth_service.model.User;
import com.boombet.auth_service.repository.UserRepository;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        log.debug("Attempting to register user with email: {}", request.email());
        
        if (userRepository.findByEmail(request.email()).isPresent()) {
            log.warn("Registration attempt with existing email: {}", request.email());
            throw new IllegalStateException("Email already taken");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setBalance(BigDecimal.ZERO);
        user.setStatus("active");
        user.setCreatedAt(OffsetDateTime.now());

        userRepository.save(user);
        log.info("User registered successfully: {}", request.email());
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        log.debug("Attempting to authenticate user: {}", request.email());
        
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        if (authentication.isAuthenticated()) {
            log.debug("Authentication successful for user: {}", request.email());
            String token = jwtService.generateToken(request.email());
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            log.info("Login successful for user: {}", request.email());
            return new LoginResponse(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getBalance(),
                user.getCreatedAt(),
                user.getStatus()
            );
        } else {
            log.warn("Authentication failed for user: {}", request.email());
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }
}
