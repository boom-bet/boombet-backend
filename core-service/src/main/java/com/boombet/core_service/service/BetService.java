package com.boombet.core_service.service;

import com.boombet.core_service.dto.PlaceBetRequest;
import com.boombet.core_service.model.*;
import com.boombet.core_service.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class BetService {

    @Autowired private UserRepository userRepository;
    @Autowired private OutcomeRepository outcomeRepository;
    @Autowired private BetRepository betRepository;
    @Autowired private BetSelectionRepository betSelectionRepository;
    @Autowired private TransactionRepository transactionRepository;

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

        return savedBet;
    }
}
