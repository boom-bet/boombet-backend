package com.boombet.auth_service.service;

import com.boombet.auth_service.dto.RegisterRequest;
import com.boombet.auth_service.model.User;
import com.boombet.auth_service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldSaveNewUser_whenEmailIsUnique() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        authService.register(request);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("hashed_password", savedUser.getPasswordHash());
        assertEquals("active", savedUser.getStatus());
    }

    @Test
    void register_shouldThrowException_whenEmailIsAlreadyTaken() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(new User()));
        assertThrows(IllegalStateException.class, () -> {
            authService.register(request);
        });
        verify(userRepository, never()).save(any(User.class));
    }
}
