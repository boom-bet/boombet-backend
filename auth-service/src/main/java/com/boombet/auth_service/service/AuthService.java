package com.boombet.auth_service.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

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

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new IllegalStateException("Email already taken");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setBalance(BigDecimal.ZERO);
        user.setStatus("active");
        user.setCreatedAt(OffsetDateTime.now());

        userRepository.save(user);
    }

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        if (authentication.isAuthenticated()) {
            String token = jwtService.generateToken(request.email());
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));
            
            return new LoginResponse(
                token,
                user.getUserId(),
                user.getEmail(),
                user.getBalance(),
                user.getCreatedAt(),
                user.getStatus()
            );
        } else {
            throw new UsernameNotFoundException("Invalid user request!");
        }
    }
}
