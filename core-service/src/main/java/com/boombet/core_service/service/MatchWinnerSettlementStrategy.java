package com.boombet.core_service.service;

import org.springframework.stereotype.Component;
import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Outcome;

@Component
public class MatchWinnerSettlementStrategy implements SettlementStrategy {
    @Override
    public boolean isOutcomeWon(Outcome outcome, Event event) {
        String result = event.getResult(); 
        String[] scores = result.split("-");
        if (scores.length < 2) return false;
        
        int homeScore = Integer.parseInt(scores[0].trim());
        int awayScore = Integer.parseInt(scores[1].trim());

        return switch (outcome.getName()) {
            case "1" -> homeScore > awayScore;
            case "X" -> homeScore == awayScore;
            case "2" -> homeScore < awayScore;
            default -> false;
        };
    }

    @Override
    public String getMarketName() {
        return "Исход матча";
    }
}
