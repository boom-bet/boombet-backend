package com.boombet.core_service.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "betselections")
@Data
public class BetSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long selectionId;

    @Column(name = "bet_id")
    private Long betId;
    
    private BigDecimal oddsAtPlacement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outcome_id")
    private Outcome outcome;

    // Explicitly adding methods to avoid Lombok issues
    public Long getBetId() { return betId; }
    public void setBetId(Long betId) { this.betId = betId; }
    
    public Long getOutcomeId() {
        return outcome != null ? outcome.getOutcomeId() : null;
    }
    
    public BigDecimal getOddsAtPlacement() { return oddsAtPlacement; }
    public void setOddsAtPlacement(BigDecimal oddsAtPlacement) { this.oddsAtPlacement = oddsAtPlacement; }
    
    public Outcome getOutcome() { return outcome; }
    public void setOutcome(Outcome outcome) { this.outcome = outcome; }
}

