package com.boombet.core_service.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.boombet.core_service.config.KafkaTopicConfig;
import com.boombet.core_service.dto.PlaceBetRequest;
import com.boombet.core_service.model.Bet;
import com.boombet.core_service.model.BetSelection;
import com.boombet.core_service.model.Outcome;
import com.boombet.core_service.model.Transaction;
import com.boombet.core_service.model.User;
import com.boombet.core_service.repository.BetRepository;
import com.boombet.core_service.repository.BetSelectionRepository;
import com.boombet.core_service.repository.OutcomeRepository;
import com.boombet.core_service.repository.TransactionRepository;
import com.boombet.core_service.repository.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class BetService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final UserRepository userRepository;
    private final OutcomeRepository outcomeRepository;
    private final BetRepository betRepository;
    private final BetSelectionRepository betSelectionRepository;
    private final TransactionRepository transactionRepository;
    private static final Logger log = LoggerFactory.getLogger(BetService.class);

    @Autowired
    public BetService( KafkaTemplate<String, String> kafkaTemplate, UserRepository userRepository, OutcomeRepository outcomeRepository, BetRepository betRepository, BetSelectionRepository betSelectionRepository,TransactionRepository transactionRepository) {
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
        this.outcomeRepository = outcomeRepository;
        this.betRepository = betRepository;
        this.betSelectionRepository = betSelectionRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public Bet placeBet(PlaceBetRequest request, String userEmail) {
        log.info("Placing bet for user: {} with outcomeIds: {}, stakeAmount: {}", 
                 userEmail, request.outcomeIds(), request.stakeAmount());
        
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + userEmail));
        if (user.getBalance().compareTo(request.stakeAmount()) < 0) {
            throw new IllegalStateException("Insufficient balance to place bet");
        }

        List<Outcome> selectedOutcomes = outcomeRepository.findAllById(request.outcomeIds());
        log.info("Found {} outcomes for bet", selectedOutcomes.size());
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
        bet.setStatus("PENDING");
        bet.setCreatedAt(OffsetDateTime.now());
        Bet savedBet = betRepository.save(bet);

        for (Outcome outcome : selectedOutcomes) {
            BetSelection selection = new BetSelection();
            selection.setBetId(savedBet.getBetId());
            selection.setOutcome(outcome); // Set the outcome object, not just the ID
            selection.setOddsAtPlacement(outcome.getCurrentOdds());
            log.info("Saving BetSelection: betId={}, outcomeId={}, odds={}", 
                     savedBet.getBetId(), outcome.getOutcomeId(), outcome.getCurrentOdds());
            betSelectionRepository.save(selection);
        }

        Transaction transaction = new Transaction.Builder()
            .userId(user.getUserId())
            .amount(request.stakeAmount().negate())
            .type("BET_PLACEMENT")
            .build();
        transactionRepository.save(transaction);

        String notificationMessage = String.format("User %s placed a new bet with ID %d!", userEmail, savedBet.getBetId());
        kafkaTemplate.send(KafkaTopicConfig.TOPIC_NOTIFICATIONS, notificationMessage);

        return savedBet;
    }

    // Deprecated: Use BetSettlementService instead
    // public void settleBetsForEvent(Event event) { ... }
}
