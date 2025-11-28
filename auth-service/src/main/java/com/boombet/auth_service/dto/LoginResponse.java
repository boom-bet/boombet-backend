package com.boombet.auth_service.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record LoginResponse(
    String token,
    Long userId,
    String email,
    BigDecimal balance,
    OffsetDateTime createdAt,
    String status
) {
}
