package com.boombet.core_service.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionDTO(
    Long transactionId,
    Long userId,
    BigDecimal amount,
    String type,
    String status,
    OffsetDateTime createdAt
) {}

