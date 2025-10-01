package com.boombet.core_service.controller;

import com.boombet.core_service.dto.PlaceBetRequest;
import com.boombet.core_service.model.Bet;
import com.boombet.core_service.service.BetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/bets")
public class BetController {

    private static final Logger log = LoggerFactory.getLogger(BetController.class);

    @Autowired
    private BetService betService;

    @PostMapping
    public ResponseEntity<?> placeBet(
            @RequestBody PlaceBetRequest request,
            HttpServletRequest httpServletRequest) {
        log.info("<<<< [CONTROLLER] Received request to place a bet.");
        log.info("<<<< [CONTROLLER] Headers found:");
        httpServletRequest.getHeaderNames().asIterator()
                .forEachRemaining(headerName -> 
                    log.info("<<<< [CONTROLLER] {}: {}", headerName, httpServletRequest.getHeader(headerName))
                );

        try {
            String userEmail = httpServletRequest.getHeader("X-Authenticated-User-Email");
            if (userEmail == null) {
                log.error("<<<< [CONTROLLER] CRITICAL: X-Authenticated-User-Email header is NULL!");
                throw new IllegalStateException("X-Authenticated-User-Email header is missing");
            }

            Bet placedBet = betService.placeBet(request, userEmail);
            return ResponseEntity.ok(placedBet);
        } catch (Exception e) {
            log.error("<<<< [CONTROLLER] Error placing bet: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
