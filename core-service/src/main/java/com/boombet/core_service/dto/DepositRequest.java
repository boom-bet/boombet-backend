package com.boombet.core_service.dto;

import java.math.BigDecimal;

public record DepositRequest(
    BigDecimal amount
) {}

