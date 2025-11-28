package com.boombet.core_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetStatsResponse {
    private long totalBets;
    private long pendingBets;
    private long wonBets;
    private long lostBets;
}

