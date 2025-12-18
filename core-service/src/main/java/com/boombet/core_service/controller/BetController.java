package com.boombet.core_service.controller;

import java.time.OffsetDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boombet.core_service.dto.BetHistoryResponse;
import com.boombet.core_service.dto.BetStatsResponse;
import com.boombet.core_service.dto.PlaceBetRequest;
import com.boombet.core_service.model.Bet;
import com.boombet.core_service.service.BetHistoryService;
import com.boombet.core_service.service.BetService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/bets")
public class BetController {

    private static final Logger log = LoggerFactory.getLogger(BetController.class);

    @Autowired
    private BetService betService;

    @Autowired
    private BetHistoryService betHistoryService;

    @PostMapping
    public ResponseEntity<?> placeBet(
            @jakarta.validation.Valid @RequestBody PlaceBetRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("Received bet placement request");

        try {
            String userEmail = httpServletRequest.getHeader("X-Authenticated-User-Email");
            if (userEmail == null) {
                log.error("Missing X-Authenticated-User-Email header");
                return ResponseEntity.status(401).body("Authentication required");
            }

            Bet placedBet = betService.placeBet(request, userEmail);
            log.info("Bet placed successfully for user: {}, betId: {}", userEmail, placedBet.getBetId());
            return ResponseEntity.ok(placedBet);
        } catch (IllegalStateException e) {
            log.warn("Bet placement failed: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error placing bet", e);
            return ResponseEntity.status(500).body("Internal server error");
        }
    }

    /**
     * Получить историю ставок пользователя
     */
    @GetMapping("/history")
    public ResponseEntity<Page<BetHistoryResponse>> getBetHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpServletRequest) {
        try {
            String userEmail = httpServletRequest.getHeader("X-Authenticated-User-Email");
            if (userEmail == null) {
                throw new IllegalStateException("Authentication required");
            }

            Page<BetHistoryResponse> history = betHistoryService.getUserBetHistory(userEmail, page, size);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting bet history: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Получить историю ставок по статусу
     */
    @GetMapping("/history/status/{status}")
    public ResponseEntity<Page<BetHistoryResponse>> getBetHistoryByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpServletRequest) {
        try {
            String userEmail = httpServletRequest.getHeader("X-Authenticated-User-Email");
            if (userEmail == null) {
                throw new IllegalStateException("Authentication required");
            }

            Page<BetHistoryResponse> history = betHistoryService.getUserBetHistoryByStatus(
                userEmail, status, page, size);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting bet history by status: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Получить историю ставок за период
     */
    @GetMapping("/history/range")
    public ResponseEntity<Page<BetHistoryResponse>> getBetHistoryByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest httpServletRequest) {
        try {
            String userEmail = httpServletRequest.getHeader("X-Authenticated-User-Email");
            if (userEmail == null) {
                throw new IllegalStateException("Authentication required");
            }

            Page<BetHistoryResponse> history = betHistoryService.getUserBetHistoryByDateRange(
                userEmail, startDate, endDate, page, size);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            log.error("Error getting bet history by date range: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Получить статистику ставок
     */
    @GetMapping("/stats")
    public ResponseEntity<BetStatsResponse> getBetStats(HttpServletRequest httpServletRequest) {
        try {
            String userEmail = httpServletRequest.getHeader("X-Authenticated-User-Email");
            if (userEmail == null) {
                throw new IllegalStateException("Authentication required");
            }

            BetStatsResponse stats = betHistoryService.getUserBetStats(userEmail);
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            log.error("Error getting bet stats: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Получить детали конкретной ставки
     */
    @GetMapping("/{betId}")
    public ResponseEntity<BetHistoryResponse> getBetDetails(
            @PathVariable Long betId,
            HttpServletRequest httpServletRequest) {
        try {
            String userEmail = httpServletRequest.getHeader("X-Authenticated-User-Email");
            if (userEmail == null) {
                throw new IllegalStateException("Authentication required");
            }

            BetHistoryResponse bet = betHistoryService.getBetDetails(userEmail, betId);
            return ResponseEntity.ok(bet);
        } catch (Exception e) {
            log.error("Error getting bet details: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Отменить ставку
     */
    @DeleteMapping("/{betId}")
    public ResponseEntity<BetHistoryResponse> cancelBet(
            @PathVariable Long betId,
            HttpServletRequest httpServletRequest) {
        try {
            String userEmail = httpServletRequest.getHeader("X-Authenticated-User-Email");
            if (userEmail == null) {
                throw new IllegalStateException("Authentication required");
            }

            BetHistoryResponse bet = betHistoryService.cancelBet(userEmail, betId);
            return ResponseEntity.ok(bet);
        } catch (Exception e) {
            log.error("Error cancelling bet: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        }
    }
}
