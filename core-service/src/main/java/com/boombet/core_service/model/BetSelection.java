package com.boombet.core_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "betselections")
@Data
public class BetSelection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long selectionId;

    private Long betId;
    private Long outcomeId;
    private BigDecimal oddsAtPlacement;

    // Explicitly adding methods to avoid Lombok issues
    public Long getBetId() { return betId; }
    public void setBetId(Long betId) { this.betId = betId; }
    public Long getOutcomeId() { return outcomeId; }
    public void setOutcomeId(Long outcomeId) { this.outcomeId = outcomeId; }
    public BigDecimal getOddsAtPlacement() { return oddsAtPlacement; }
    public void setOddsAtPlacement(BigDecimal oddsAtPlacement) { this.oddsAtPlacement = oddsAtPlacement; }
}
