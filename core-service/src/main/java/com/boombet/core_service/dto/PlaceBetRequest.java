package com.boombet.core_service.dto;

import java.math.BigDecimal;
import java.util.List;

public record PlaceBetRequest(
    BigDecimal stakeAmount,
    List<Long> outcomeIds
) {}
