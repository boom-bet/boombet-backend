package com.boombet.core_service.service;

import org.springframework.stereotype.Component;

import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Outcome;

@Component
public class MatchWinnerSettlementStrategy implements SettlementStrategy {

    private static final String MARKET_NAME = "Match Winner";
    private static final String DRAW_LABEL = "Draw";

    @Override
    public boolean isOutcomeWon(Outcome outcome, Event event) {
        String result = event.getResult();
        if (result == null || result.isBlank()) {
            return false;
        }

        String[] scores = result.split("-");
        if (scores.length < 2) {
            return false;
        }

        int homeScore;
        int awayScore;
        try {
            homeScore = Integer.parseInt(scores[0].trim());
            awayScore = Integer.parseInt(scores[1].trim());
        } catch (NumberFormatException ex) {
            return false;
        }

        String outcomeName = outcome.getName();
        if (outcomeName == null || outcomeName.isBlank()) {
            return false;
        }

        boolean homeTeamWins = homeScore > awayScore;
        boolean awayTeamWins = homeScore < awayScore;

        if (nameMatches(outcomeName, event.getTeamA(), "1")) {
            return homeTeamWins;
        }
        if (nameMatches(outcomeName, event.getTeamB(), "2")) {
            return awayTeamWins;
        }
        if (nameMatches(outcomeName, DRAW_LABEL, "X")) {
            return homeScore == awayScore;
        }
        return false;
    }

    @Override
    public String getMarketName() {
        return MARKET_NAME;
    }

    private boolean nameMatches(String outcomeName, String eventName, String legacyCode) {
        if (eventName != null && outcomeName.equalsIgnoreCase(eventName)) {
            return true;
        }
        return legacyCode != null && outcomeName.equalsIgnoreCase(legacyCode);
    }
}
