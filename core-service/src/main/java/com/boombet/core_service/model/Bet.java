package com.boombet.core_service.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Data;

@Entity
@Table(name = "bets")
@Data
public class Bet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long betId;
    
    private Long userId;
    private BigDecimal stakeAmount;
    private BigDecimal totalOdds;
    private BigDecimal potentialPayout;
    private String status;
    private OffsetDateTime createdAt;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "bet_id", insertable = false, updatable = false)
    private List<BetSelection> betSelections;

    @Transient
    public User getUser() {
        // Будем загружать User через UserRepository по userId при необходимости
        return null;
    }

    // Explicitly adding methods to avoid Lombok issues
    public Long getBetId() { return this.betId; }
    public Long getUserId() { return this.userId; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getPotentialPayout() { return this.potentialPayout; }
    public List<BetSelection> getBetSelections() { return this.betSelections; }
}

