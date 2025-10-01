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
}
