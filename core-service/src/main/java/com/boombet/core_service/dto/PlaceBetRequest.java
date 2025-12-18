package com.boombet.core_service.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PlaceBetRequest(
    @NotNull(message = "Stake amount is required")
    @DecimalMin(value = "1.0", message = "Minimum bet amount is 1.0")
    BigDecimal stakeAmount,
    
    @NotEmpty(message = "At least one outcome must be selected")
    List<Long> outcomeIds
) {}
