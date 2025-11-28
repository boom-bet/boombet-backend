package com.boombet.core_service.service;

import com.boombet.core_service.dto.TransactionDTO;
import com.boombet.core_service.dto.UserProfileDTO;
import com.boombet.core_service.model.Transaction;
import com.boombet.core_service.model.User;
import com.boombet.core_service.repository.TransactionRepository;
import com.boombet.core_service.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private UserProfileService userProfileService;

    private User testUser;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUserId(1L);
        testUser.setEmail("test@example.com");
        testUser.setBalance(new BigDecimal("1000.00"));

        testTransaction = new Transaction.Builder()
                .userId(1L)
                .amount(new BigDecimal("500.00"))
                .type("deposit")
                .build();
    }

    @Test
    void testGetUserProfile_Success() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        UserProfileDTO profile = userProfileService.getUserProfile("test@example.com");

        assertNotNull(profile);
        assertEquals(1L, profile.userId());
        assertEquals("test@example.com", profile.email());
        assertEquals(new BigDecimal("1000.00"), profile.balance());
    }

    @Test
    void testGetUserProfile_UserNotFound() {
        when(userRepository.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> userProfileService.getUserProfile("notfound@example.com")
        );

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testGetUserBalance_Success() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        BigDecimal balance = userProfileService.getUserBalance("test@example.com");

        assertEquals(new BigDecimal("1000.00"), balance);
    }

    @Test
    void testGetUserTransactions_Success() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(testTransaction));

        List<TransactionDTO> transactions = userProfileService.getUserTransactions("test@example.com");

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals("deposit", transactions.getFirst().type());
    }

    @Test
    void testDeposit_Success() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);
        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(testTransaction);

        TransactionDTO result = userProfileService.deposit("test@example.com", new BigDecimal("500.00"));

        assertNotNull(result);
        assertEquals("deposit", result.type());
        assertEquals(new BigDecimal("500.00"), result.amount());
        verify(userRepository).save(any(User.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testDeposit_NegativeAmount() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userProfileService.deposit("test@example.com", new BigDecimal("-100.00"))
        );

        assertTrue(exception.getMessage().contains("must be positive"));
    }

    @Test
    void testWithdraw_Success() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenReturn(testUser);

        Transaction withdrawalTransaction = new Transaction.Builder()
                .userId(1L)
                .amount(new BigDecimal("-200.00"))
                .type("withdrawal")
                .build();

        when(transactionRepository.save(any(Transaction.class)))
                .thenReturn(withdrawalTransaction);

        TransactionDTO result = userProfileService.withdraw("test@example.com", new BigDecimal("200.00"));

        assertNotNull(result);
        assertEquals("withdrawal", result.type());
        verify(userRepository).save(any(User.class));
        verify(transactionRepository).save(any(Transaction.class));
    }

    @Test
    void testWithdraw_InsufficientBalance() {
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(testUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userProfileService.withdraw("test@example.com", new BigDecimal("5000.00"))
        );

        assertTrue(exception.getMessage().contains("Insufficient balance"));
    }

    @Test
    void testWithdraw_NegativeAmount() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userProfileService.withdraw("test@example.com", new BigDecimal("-100.00"))
        );

        assertTrue(exception.getMessage().contains("must be positive"));
    }
}
