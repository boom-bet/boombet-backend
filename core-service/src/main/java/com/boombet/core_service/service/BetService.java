package com.boombet.core_service.service;

import com.boombet.core_service.config.KafkaTopicConfig;
import com.boombet.core_service.dto.PlaceBetRequest;
import com.boombet.core_service.model.*;
import com.boombet.core_service.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BetService {
    @Autowired private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired private UserRepository userRepository;
    @Autowired private OutcomeRepository outcomeRepository;
    @Autowired private BetRepository betRepository;
    @Autowired private BetSelectionRepository betSelectionRepository;
    @Autowired private TransactionRepository transactionRepository;
    private static final Logger log = LoggerFactory.getLogger(BetService.class);

    @Transactional
    public Bet placeBet(PlaceBetRequest request, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        if (user.getBalance().compareTo(request.stakeAmount()) < 0) {
            throw new IllegalStateException("Insufficient balance to place bet");
        }

        List<Outcome> selectedOutcomes = outcomeRepository.findAllById(request.outcomeIds());
        if (selectedOutcomes.size() != request.outcomeIds().size()) {
            throw new IllegalArgumentException("One or more outcomes not found");
        }

        BigDecimal totalOdds = selectedOutcomes.stream()
                .map(Outcome::getCurrentOdds)
                .reduce(BigDecimal.ONE, BigDecimal::multiply);

        BigDecimal potentialPayout = request.stakeAmount().multiply(totalOdds);

        user.setBalance(user.getBalance().subtract(request.stakeAmount()));
        userRepository.save(user);

        Bet bet = new Bet();
        bet.setUserId(user.getUserId());
        bet.setStakeAmount(request.stakeAmount());
        bet.setTotalOdds(totalOdds);
        bet.setPotentialPayout(potentialPayout);
        bet.setStatus("pending");
        bet.setCreatedAt(OffsetDateTime.now());
        Bet savedBet = betRepository.save(bet);

        for (Outcome outcome : selectedOutcomes) {
            BetSelection selection = new BetSelection();
            selection.setBetId(savedBet.getBetId());
            selection.setOutcomeId(outcome.getOutcomeId());
            selection.setOddsAtPlacement(outcome.getCurrentOdds());
            betSelectionRepository.save(selection);
        }

        Transaction transaction = new Transaction();
        transaction.setUserId(user.getUserId());
        transaction.setAmount(request.stakeAmount().negate());
        transaction.setType("BET_PLACEMENT");
        transaction.setStatus("completed");
        transaction.setCreatedAt(OffsetDateTime.now());
        transactionRepository.save(transaction);

        String notificationMessage = String.format("User %s placed a new bet with ID %d!", userEmail, savedBet.getBetId());
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_NOTIFICATIONS, notificationMessage);

        return savedBet;
    }

    @Transactional
    public void settleBetsForEvent(Event event) {
        log.info("Settling bets for finished event: {}", event.getExternalId());

        // 1. Находим все ставки, которые нужно рассчитать
        List<Bet> betsToSettle = betRepository.findPendingBetsByEventId(event.getEventId());

        for (Bet bet : betsToSettle) {
            // TODO: Проверить, все ли исходы в этой ставке относятся к завершенным событиям.
            // Для экспресс-ставок это важно. Пока мы упрощаем и считаем, что все.

            boolean isBetWon = true; // Предпологаем, что ставка выиграла
            List<BetSelection> selections = betSelectionRepository.findAllByBetId(bet.getBetId());

            for (BetSelection selection : selections) {
                Outcome outcome = outcomeRepository.findById(selection.getOutcomeId()).orElseThrow();
                // TODO: Здесь нужна более сложная логика определения выигрыша.
                // Например, если результат матча "2-1", то исход "П1" является выигрышным.
                // Пока мы делаем очень простое предположение для примера.
                // Например, если результат матча содержит название исхода.
                if (!event.getResult().contains(outcome.getName())) {
                     //isBetWon = false; // Упрощенная логика - пока закомментировано
                     // break;
                }
            }

            // ВРЕМЕННО: для демонстрации будем считать, что все ставки выигрывают
            if (isBetWon) {
                log.info("Bet ID {} has WON!", bet.getBetId());
                bet.setStatus("won");
                
                User user = userRepository.findById(bet.getUserId()).orElseThrow();
                user.setBalance(user.getBalance().add(bet.getPotentialPayout()));
                userRepository.save(user);

                Transaction transaction = new Transaction();
                transaction.setUserId(user.getUserId());
                transaction.setAmount(bet.getPotentialPayout());
                transaction.setType("BET_WINNING");
                transaction.setStatus("completed");
                transaction.setCreatedAt(OffsetDateTime.now());
                transactionRepository.save(transaction);
                
            } else {
                log.info("Bet ID {} has LOST.", bet.getBetId());
                bet.setStatus("lost");
            }
            betRepository.save(bet);
        }
    }
}
