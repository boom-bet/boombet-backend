package com.boombet.core_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

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
}
