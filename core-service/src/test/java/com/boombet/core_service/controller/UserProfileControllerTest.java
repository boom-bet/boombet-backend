package com.boombet.core_service.controller;

import com.boombet.core_service.dto.DepositRequest;
import com.boombet.core_service.dto.TransactionDTO;
import com.boombet.core_service.dto.UserProfileDTO;
import com.boombet.core_service.dto.WithdrawalRequest;
import com.boombet.core_service.service.UserProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserProfileController.class)
public class UserProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserProfileService userProfileService;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String AUTH_HEADER = "X-Authenticated-User-Email";

    private UserProfileDTO testProfile;
    private TransactionDTO testTransaction;

    @BeforeEach
    void setUp() {
        testProfile = new UserProfileDTO(1L, TEST_EMAIL, new BigDecimal("1000.00"));
        testTransaction = new TransactionDTO(
                1L,
                1L,
                new BigDecimal("500.00"),
                "deposit",
                "completed",
                OffsetDateTime.now()
        );
    }

    @Test
    void testGetUserProfile_Success() throws Exception {
        when(userProfileService.getUserProfile(TEST_EMAIL)).thenReturn(testProfile);

        mockMvc.perform(get("/api/v1/users/profile")
                .header(AUTH_HEADER, TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.email").value(TEST_EMAIL))
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    void testGetUserProfile_MissingAuthHeader() throws Exception {
        mockMvc.perform(get("/api/v1/users/profile"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUserBalance_Success() throws Exception {
        when(userProfileService.getUserBalance(TEST_EMAIL))
                .thenReturn(new BigDecimal("1000.00"));

        mockMvc.perform(get("/api/v1/users/balance")
                .header(AUTH_HEADER, TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(1000.00));
    }

    @Test
    void testGetUserTransactions_Success() throws Exception {
        List<TransactionDTO> transactions = List.of(testTransaction);
        when(userProfileService.getUserTransactions(TEST_EMAIL)).thenReturn(transactions);

        mockMvc.perform(get("/api/v1/users/transactions")
                .header(AUTH_HEADER, TEST_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].transactionId").value(1))
                .andExpect(jsonPath("$[0].type").value("deposit"))
                .andExpect(jsonPath("$[0].amount").value(500.00));
    }

    @Test
    void testDeposit_Success() throws Exception {
        DepositRequest request = new DepositRequest(new BigDecimal("500.00"));
        when(userProfileService.deposit(eq(TEST_EMAIL), any(BigDecimal.class)))
                .thenReturn(testTransaction);

        mockMvc.perform(post("/api/v1/users/deposit")
                .header(AUTH_HEADER, TEST_EMAIL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(500.00))
                .andExpect(jsonPath("$.type").value("deposit"));
    }

    @Test
    void testDeposit_InvalidAmount() throws Exception {
        DepositRequest request = new DepositRequest(new BigDecimal("-100.00"));
        when(userProfileService.deposit(eq(TEST_EMAIL), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Deposit amount must be positive"));

        mockMvc.perform(post("/api/v1/users/deposit")
                .header(AUTH_HEADER, TEST_EMAIL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testWithdraw_Success() throws Exception {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("200.00"));
        TransactionDTO withdrawalTransaction = new TransactionDTO(
                2L,
                1L,
                new BigDecimal("-200.00"),
                "withdrawal",
                "completed",
                OffsetDateTime.now()
        );
        when(userProfileService.withdraw(eq(TEST_EMAIL), any(BigDecimal.class)))
                .thenReturn(withdrawalTransaction);

        mockMvc.perform(post("/api/v1/users/withdraw")
                .header(AUTH_HEADER, TEST_EMAIL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(-200.00))
                .andExpect(jsonPath("$.type").value("withdrawal"));
    }

    @Test
    void testWithdraw_InsufficientBalance() throws Exception {
        WithdrawalRequest request = new WithdrawalRequest(new BigDecimal("5000.00"));
        when(userProfileService.withdraw(eq(TEST_EMAIL), any(BigDecimal.class)))
                .thenThrow(new IllegalArgumentException("Insufficient balance"));

        mockMvc.perform(post("/api/v1/users/withdraw")
                .header(AUTH_HEADER, TEST_EMAIL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

