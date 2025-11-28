package com.boombet.core_service.service;

import com.boombet.core_service.dto.BetHistoryResponse;
import com.boombet.core_service.dto.BetStatsResponse;
import com.boombet.core_service.model.*;
import com.boombet.core_service.repository.BetRepository;
import com.boombet.core_service.repository.BetSelectionRepository;
import com.boombet.core_service.repository.OutcomeRepository;
import com.boombet.core_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BetHistoryService {

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private BetSelectionRepository betSelectionRepository;

    @Autowired
    private OutcomeRepository outcomeRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Получить историю ставок пользователя с пагинацией
     */
    @Transactional(readOnly = true)
    public Page<BetHistoryResponse> getUserBetHistory(String userEmail, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Bet> bets = betRepository.findByUserIdOrderByCreatedAtDesc(user.getUserId(), pageable);

        return bets.map(this::convertToBetHistoryResponse);
    }

    /**
     * Получить историю ставок по статусу
     */
    @Transactional(readOnly = true)
    public Page<BetHistoryResponse> getUserBetHistoryByStatus(String userEmail, String status, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Bet> bets = betRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
            user.getUserId(), status, pageable);

        return bets.map(this::convertToBetHistoryResponse);
    }

    /**
     * Получить историю ставок за период
     */
    @Transactional(readOnly = true)
    public Page<BetHistoryResponse> getUserBetHistoryByDateRange(
            String userEmail, OffsetDateTime startDate, OffsetDateTime endDate, int page, int size) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Pageable pageable = PageRequest.of(page, size);
        Page<Bet> bets = betRepository.findByUserIdAndDateRange(
            user.getUserId(), startDate, endDate, pageable);

        return bets.map(this::convertToBetHistoryResponse);
    }

    /**
     * Получить статистику ставок пользователя
     */
    @Transactional(readOnly = true)
    public BetStatsResponse getUserBetStats(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        long totalBets = betRepository.countByUserId(user.getUserId());
        long pendingBets = betRepository.countByUserIdAndStatus(user.getUserId(), "pending");
        long wonBets = betRepository.countByUserIdAndStatus(user.getUserId(), "won");
        long lostBets = betRepository.countByUserIdAndStatus(user.getUserId(), "lost");

        return new BetStatsResponse(totalBets, pendingBets, wonBets, lostBets);
    }

    /**
     * Отменить ставку (только pending)
     */
    @Transactional
    public BetHistoryResponse cancelBet(String userEmail, Long betId) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Bet bet = betRepository.findById(betId)
            .orElseThrow(() -> new RuntimeException("Bet not found"));

        // Проверка, что ставка принадлежит пользователю
        if (!bet.getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Bet does not belong to this user");
        }

        // Можно отменить только pending ставки
        if (!"pending".equals(bet.getStatus())) {
            throw new RuntimeException("Can only cancel pending bets");
        }

        // Возврат средств
        user.setBalance(user.getBalance().add(bet.getStakeAmount()));
        userRepository.save(user);

        // Обновление статуса ставки
        bet.setStatus("cancelled");
        bet = betRepository.save(bet);

        return convertToBetHistoryResponse(bet);
    }

    /**
     * Получить детали конкретной ставки
     */
    @Transactional(readOnly = true)
    public BetHistoryResponse getBetDetails(String userEmail, Long betId) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        Bet bet = betRepository.findById(betId)
            .orElseThrow(() -> new RuntimeException("Bet not found"));

        // Проверка, что ставка принадлежит пользователю
        if (!bet.getUserId().equals(user.getUserId())) {
            throw new RuntimeException("Bet does not belong to this user");
        }

        return convertToBetHistoryResponse(bet);
    }

    /**
     * Конвертация Bet в BetHistoryResponse
     */
    private BetHistoryResponse convertToBetHistoryResponse(Bet bet) {
        List<BetSelection> selections = betSelectionRepository.findAllByBetId(bet.getBetId());

        List<BetHistoryResponse.BetSelectionInfo> selectionInfos = selections.stream()
            .map(selection -> {
                Outcome outcome = outcomeRepository.findById(selection.getOutcomeId())
                    .orElse(null);

                if (outcome != null) {
                    Market market = outcome.getMarket();
                    Event event = market.getEvent();

                    String eventName = event.getTeamA() + " vs " + event.getTeamB();

                    return new BetHistoryResponse.BetSelectionInfo(
                        outcome.getOutcomeId(),
                        eventName,
                        market.getName(),
                        outcome.getName(),
                        selection.getOddsAtPlacement()
                    );
                }
                return null;
            })
            .filter(info -> info != null)
            .collect(Collectors.toList());

        return BetHistoryResponse.fromBet(bet, selectionInfos);
    }
}

