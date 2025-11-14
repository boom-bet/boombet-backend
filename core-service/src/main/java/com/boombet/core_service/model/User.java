package com.boombet.core_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    private Long userId;
    private String email;
    private BigDecimal balance;

    // Explicitly adding methods to avoid Lombok issues
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}
