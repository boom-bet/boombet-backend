package com.boombet.core_service.service;

import com.boombet.core_service.dto.TransactionDTO;
import com.boombet.core_service.dto.UserProfileDTO;
import com.boombet.core_service.model.Transaction;
import com.boombet.core_service.model.User;
import com.boombet.core_service.repository.TransactionRepository;
import com.boombet.core_service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserProfileService {

    private static final Logger log = LoggerFactory.getLogger(UserProfileService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Получить профиль пользователя по email
     */
    public UserProfileDTO getUserProfile(String email) {
        log.info("Getting user profile for email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return new UserProfileDTO(
                user.getUserId(),
                user.getEmail(),
                user.getBalance()
        );
    }

    /**
     * Получить баланс пользователя
     */
    public BigDecimal getUserBalance(String email) {
        log.info("Getting balance for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        return user.getBalance();
    }

    /**
     * Получить историю транзакций пользователя
     */
    public List<TransactionDTO> getUserTransactions(String email) {
        log.info("Getting transactions for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId());

        return transactions.stream()
                .map(t -> new TransactionDTO(
                        t.getTransactionId(),
                        t.getUserId(),
                        t.getAmount(),
                        t.getType(),
                        t.getStatus(),
                        t.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Пополнение баланса пользователя
     */
    @Transactional
    public TransactionDTO deposit(String email, BigDecimal amount) {
        log.info("Processing deposit for user: {}, amount: {}", email, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit amount must be positive");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Обновляем баланс
        BigDecimal newBalance = user.getBalance().add(amount);
        user.setBalance(newBalance);
        userRepository.save(user);

        // Создаем транзакцию
        Transaction transaction = new Transaction.Builder()
                .userId(user.getUserId())
                .amount(amount)
                .type("deposit")
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Deposit completed. New balance: {}", newBalance);

        return new TransactionDTO(
                transaction.getTransactionId(),
                transaction.getUserId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }

    /**
     * Вывод средств с баланса пользователя
     */
    @Transactional
    public TransactionDTO withdraw(String email, BigDecimal amount) {
        log.info("Processing withdrawal for user: {}, amount: {}", email, amount);

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be positive");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        // Проверяем достаточность средств
        if (user.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient balance. Current balance: " + user.getBalance());
        }

        // Обновляем баланс
        BigDecimal newBalance = user.getBalance().subtract(amount);
        user.setBalance(newBalance);
        userRepository.save(user);

        // Создаем транзакцию
        Transaction transaction = new Transaction.Builder()
                .userId(user.getUserId())
                .amount(amount.negate()) // Отрицательная сумма для вывода
                .type("withdrawal")
                .build();

        transaction = transactionRepository.save(transaction);

        log.info("Withdrawal completed. New balance: {}", newBalance);

        return new TransactionDTO(
                transaction.getTransactionId(),
                transaction.getUserId(),
                transaction.getAmount(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getCreatedAt()
        );
    }
}

