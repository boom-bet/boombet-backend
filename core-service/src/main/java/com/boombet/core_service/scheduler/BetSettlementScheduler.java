package com.boombet.core_service.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boombet.core_service.service.BetSettlementService;

@Component
public class BetSettlementScheduler {

    private static final Logger logger = LoggerFactory.getLogger(BetSettlementScheduler.class);

    @Autowired
    private BetSettlementService betSettlementService;

    /**
     * Каждые 30 секунд проверяем завершенные события и рассчитываем ставки
     */
    @Scheduled(fixedRate = 30000) // 30 секунд
    public void settleBets() {
        logger.info("Starting bet settlement check...");
        
        try {
            betSettlementService.settleAllFinishedEvents();
            logger.info("Bet settlement check completed");
        } catch (Exception e) {
            logger.error("Error during bet settlement: {}", e.getMessage(), e);
        }
    }
}
