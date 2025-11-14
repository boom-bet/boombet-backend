package com.boombet.core_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "outcomes")
@Data
public class Outcome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "outcome_id")
    private Long outcomeId;

    private String name;

    @Column(name = "current_odds")
    private BigDecimal currentOdds;

    @Column(name = "is_active")
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "market_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Market market;

    // Explicitly adding methods to avoid Lombok issues
    public Long getOutcomeId() { return outcomeId; }
    public void setOutcomeId(Long outcomeId) { this.outcomeId = outcomeId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getCurrentOdds() { return currentOdds; }
    public void setCurrentOdds(BigDecimal currentOdds) { this.currentOdds = currentOdds; }
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
    public Market getMarket() { return market; }
    public void setMarket(Market market) { this.market = market; }
}
