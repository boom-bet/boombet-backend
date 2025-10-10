package com.boombet.core_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "transactions")
@Data
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;
    
    private Long userId;
    private BigDecimal amount;
    private String type;
    private String status;
    private OffsetDateTime createdAt;

    public static class Builder {
        private Long userId;
        private BigDecimal amount;
        private String type;
        
        public Builder userId(Long userId) { this.userId = userId; return this; }
        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder type(String type) { this.type = type; return this; }
        
        public Transaction build() {
            Transaction transaction = new Transaction();
            transaction.setUserId(this.userId);
            transaction.setAmount(this.amount);
            transaction.setType(this.type);
            transaction.setStatus("completed");
            transaction.setCreatedAt(OffsetDateTime.now());
            return transaction;
        }
    }
}
