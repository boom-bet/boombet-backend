package com.boombet.core_service.service;

import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Outcome;

public interface SettlementStrategy {
    boolean isOutcomeWon(Outcome outcome, Event event);
    String getMarketName();
}
