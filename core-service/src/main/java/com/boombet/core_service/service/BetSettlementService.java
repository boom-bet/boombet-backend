package com.boombet.core_service.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.boombet.core_service.model.Bet;
import com.boombet.core_service.model.BetSelection;
import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Market;
import com.boombet.core_service.model.Outcome;
import com.boombet.core_service.model.Transaction;
import com.boombet.core_service.model.User;
import com.boombet.core_service.repository.BetRepository;
import com.boombet.core_service.repository.EventRepository;
import com.boombet.core_service.repository.MarketRepository;
import com.boombet.core_service.repository.OutcomeRepository;
import com.boombet.core_service.repository.TransactionRepository;
import com.boombet.core_service.repository.UserRepository;

@Service
public class BetSettlementService {

    private static final Logger logger = LoggerFactory.getLogger(BetSettlementService.class);

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private OutcomeRepository outcomeRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private EventRepository eventRepository;

    /**
     * Рассчитать все pending ставки для завершенного события
     */
    @Transactional
    public void settleBetsForEvent(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (!"finished".equalsIgnoreCase(event.getStatus())) {
            logger.warn("Trying to settle bets for non-finished event: {}", eventId);
            return;
        }

        if (event.getResult() == null || event.getResult().isEmpty()) {
            logger.warn("Event {} has no result, cannot settle bets", eventId);
            return;
        }

        // Найти все pending ставки на этот event
        logger.info("Looking for pending bets for event {} using findPendingBetsByEventId", eventId);
        List<Bet> pendingBets = betRepository.findPendingBetsByEventId(eventId);
        logger.info("Found {} bets from repository", pendingBets != null ? pendingBets.size() : 0);
        
        if (pendingBets == null || pendingBets.isEmpty()) {
            logger.info("No pending bets found for event {}", eventId);
            return;
        }

        logger.info("Settling {} pending bets for event {}", pendingBets.size(), eventId);

        // Определить выигрышные исходы на основе результата
        Set<Long> winningOutcomeIds = determineWinningOutcomes(event);

        for (Bet bet : pendingBets) {
            settleBet(bet, winningOutcomeIds);
        }

        // Обновить статус события на "settled"
        event.setStatus("settled");
        eventRepository.save(event);

        logger.info("Successfully settled {} bets for event {}", pendingBets.size(), eventId);
    }

    /**
     * Рассчитать одну ставку
     */
    private void settleBet(Bet bet, Set<Long> winningOutcomeIds) {
        List<BetSelection> betSelections = bet.getBetSelections();

        // Проверить все ли исходы выиграли (для экспресса все должны быть правильными)
        boolean allWon = betSelections.stream()
                .allMatch(selection -> winningOutcomeIds.contains(selection.getOutcomeId()));

        if (allWon) {
            // Ставка выиграла
            bet.setStatus("WON");
            
            // Начислить выигрыш пользователю
            User user = userRepository.findById(bet.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            BigDecimal currentBalance = user.getBalance();
            BigDecimal newBalance = currentBalance.add(bet.getPotentialPayout());
            user.setBalance(newBalance);
            userRepository.save(user);

            // Создать транзакцию выигрыша
            Transaction transaction = new Transaction.Builder()
                    .userId(bet.getUserId())
                    .amount(bet.getPotentialPayout())
                    .type("WIN")
                    .build();
            transactionRepository.save(transaction);

            logger.info("Bet {} WON. User {} received {}", bet.getBetId(), bet.getUserId(), bet.getPotentialPayout());
        } else {
            // Ставка проиграла
            bet.setStatus("LOST");
            logger.info("Bet {} LOST", bet.getBetId());
        }

        betRepository.save(bet);
    }

    /**
     * Определить выигрышные исходы на основе результата события
     */
    private Set<Long> determineWinningOutcomes(Event event) {
        String result = event.getResult(); // например "2:1"
        
        // Парсинг результата
        String[] scoreParts = result.split(":");
        if (scoreParts.length != 2) {
            logger.warn("Invalid result format for event {}: {}", event.getEventId(), result);
            return Set.of();
        }

        int scoreA = Integer.parseInt(scoreParts[0].trim());
        int scoreB = Integer.parseInt(scoreParts[1].trim());
        int totalGoals = scoreA + scoreB;

        // Найти все исходы для этого события через MarketRepository
        List<Market> markets = marketRepository.findAllByEvent_EventId(event.getEventId());
        Set<Long> winningOutcomeIds = markets.stream()
                .flatMap(market -> market.getOutcomes().stream())
                .filter(outcome -> isWinningOutcome(outcome, market -> market.getName(), scoreA, scoreB, totalGoals))
                .map(Outcome::getOutcomeId)
                .collect(Collectors.toSet());

        logger.info("Event {} result: {}. Winning outcomes: {}", event.getEventId(), result, winningOutcomeIds);
        
        return winningOutcomeIds;
    }

    /**
     * Определить выиграл ли конкретный исход
     */
    private boolean isWinningOutcome(Outcome outcome, java.util.function.Function<Market, String> getMarketName, 
                                     int scoreA, int scoreB, int totalGoals) {
        Market market = outcome.getMarket();
        String marketName = market.getName();
        String outcomeName = outcome.getName();

        // Match Winner
        if ("Match Winner".equals(marketName)) {
            if (outcomeName.contains("Team A") || outcomeName.contains(market.getEvent().getTeamA())) {
                return scoreA > scoreB;
            } else if (outcomeName.contains("Team B") || outcomeName.contains(market.getEvent().getTeamB())) {
                return scoreB > scoreA;
            } else if (outcomeName.contains("Draw")) {
                return scoreA == scoreB;
            }
        }

        // Total Goals
        if ("Total Goals".equals(marketName)) {
            if (outcomeName.contains("Over")) {
                String thresholdStr = outcomeName.replaceAll("[^0-9.]", "");
                double threshold = Double.parseDouble(thresholdStr);
                return totalGoals > threshold;
            } else if (outcomeName.contains("Under")) {
                String thresholdStr = outcomeName.replaceAll("[^0-9.]", "");
                double threshold = Double.parseDouble(thresholdStr);
                return totalGoals < threshold;
            }
        }

        return false;
    }

    /**
     * Рассчитать все pending ставки для всех завершенных событий
     */
    @Transactional
    public void settleAllFinishedEvents() {
        List<Event> finishedEvents = eventRepository.findByStatus("finished");
        
        logger.info("Found {} finished events to settle", finishedEvents.size());
        
        for (Event event : finishedEvents) {
            try {
                settleBetsForEvent(event.getEventId());
            } catch (Exception e) {
                logger.error("Error settling bets for event {}: {}", event.getEventId(), e.getMessage(), e);
            }
        }
    }
}
