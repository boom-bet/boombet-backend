package com.boombet.core_service.controller;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.boombet.core_service.dto.DepositRequest;
import com.boombet.core_service.dto.TransactionDTO;
import com.boombet.core_service.dto.UserProfileDTO;
import com.boombet.core_service.dto.WithdrawalRequest;
import com.boombet.core_service.service.UserProfileService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/users")
public class UserProfileController {

    private static final Logger log = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired
    private UserProfileService userProfileService;

    /**
     * Получить профиль текущего пользователя (алиас для /profile)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        return getUserProfile(request);
    }

    /**
     * Получить профиль текущего пользователя
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile(HttpServletRequest request) {
        log.info("Received request to get user profile");

        try {
            String userEmail = getUserEmailFromRequest(request);
            UserProfileDTO profile = userProfileService.getUserProfile(userEmail);
            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            log.error("Error getting user profile: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Получить баланс текущего пользователя
     */
    @GetMapping("/balance")
    public ResponseEntity<?> getUserBalance(HttpServletRequest request) {
        log.info("Received request to get user balance");

        try {
            String userEmail = getUserEmailFromRequest(request);
            BigDecimal balance = userProfileService.getUserBalance(userEmail);
            return ResponseEntity.ok(new BalanceResponse(balance));
        } catch (Exception e) {
            log.error("Error getting user balance: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Получить историю транзакций текущего пользователя
     */
    @GetMapping("/transactions")
    public ResponseEntity<?> getUserTransactions(HttpServletRequest request) {
        log.info("Received request to get user transactions");

        try {
            String userEmail = getUserEmailFromRequest(request);
            List<TransactionDTO> transactions = userProfileService.getUserTransactions(userEmail);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            log.error("Error getting user transactions: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Пополнить баланс
     */
    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(
            @RequestBody DepositRequest depositRequest,
            HttpServletRequest request) {
        log.info("Received request to deposit funds: {}", depositRequest.amount());

        try {
            String userEmail = getUserEmailFromRequest(request);
            TransactionDTO transaction = userProfileService.deposit(userEmail, depositRequest.amount());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Error processing deposit: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Вывести средства
     */
    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(
            @RequestBody WithdrawalRequest withdrawalRequest,
            HttpServletRequest request) {
        log.info("Received request to withdraw funds: {}", withdrawalRequest.amount());

        try {
            String userEmail = getUserEmailFromRequest(request);
            TransactionDTO transaction = userProfileService.withdraw(userEmail, withdrawalRequest.amount());
            return ResponseEntity.ok(transaction);
        } catch (Exception e) {
            log.error("Error processing withdrawal: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Извлечь email пользователя из заголовка запроса
     */
    private String getUserEmailFromRequest(HttpServletRequest request) {
        String userEmail = request.getHeader("X-Authenticated-User-Email");
        if (userEmail == null || userEmail.isEmpty()) {
            log.error("X-Authenticated-User-Email header is missing");
            throw new IllegalStateException("User authentication required");
        }
        return userEmail;
    }

    /**
     * Внутренний класс для ответа с балансом
     */
    private record BalanceResponse(BigDecimal balance) {}
}

