package com.boombet.realtime_service.dto;

import java.math.BigDecimal;

public record OutcomeDTO(
    String name,
    BigDecimal price
) {}
