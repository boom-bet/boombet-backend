package com.boombet.core_service.dto;

import java.math.BigDecimal;

public record UserProfileDTO(
    Long userId,
    String email,
    BigDecimal balance
) {}

