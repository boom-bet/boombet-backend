package com.boombet.core_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "markets")
@Data
public class Market {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "market_id")
    private Long marketId;
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    @ToString.Exclude
    private Event event;
    @OneToMany(mappedBy = "market", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Outcome> outcomes;

    // Explicitly adding methods to avoid Lombok issues
    public Long getMarketId() { return marketId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public List<Outcome> getOutcomes() { return outcomes; }
    public void setOutcomes(List<Outcome> outcomes) { this.outcomes = outcomes; }
}
