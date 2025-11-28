package com.boombet.core_service.dto;

import com.boombet.core_service.model.Bet;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BetHistoryResponse {
    private Long betId;
    private BigDecimal stakeAmount;
    private BigDecimal totalOdds;
    private BigDecimal potentialPayout;
    private String status;
    private OffsetDateTime createdAt;
    private List<BetSelectionInfo> selections;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class BetSelectionInfo {
        private Long outcomeId;
        private String eventName;
        private String marketType;
        private String outcomeName;
        private BigDecimal odds;
    }

    public static BetHistoryResponse fromBet(Bet bet, List<BetSelectionInfo> selections) {
        BetHistoryResponse response = new BetHistoryResponse();
        response.setBetId(bet.getBetId());
        response.setStakeAmount(bet.getStakeAmount());
        response.setTotalOdds(bet.getTotalOdds());
        response.setPotentialPayout(bet.getPotentialPayout());
        response.setStatus(bet.getStatus());
        response.setCreatedAt(bet.getCreatedAt());
        response.setSelections(selections);
        return response;
    }
}

