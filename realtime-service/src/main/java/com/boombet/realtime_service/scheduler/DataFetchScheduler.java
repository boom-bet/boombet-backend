package com.boombet.realtime_service.scheduler;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.boombet.realtime_service.dto.MarketDTO;
import com.boombet.realtime_service.dto.MatchUpdateDTO;
import com.boombet.realtime_service.dto.OutcomeDTO;

@Component
public class DataFetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataFetchScheduler.class);
    public static final String TOPIC_EVENTS_UPDATE = "events-updates";
    private final Random random = new Random();
    
    // Храним активные матчи, чтобы обновлять их, а не создавать новые
    private final Map<String, MatchUpdateDTO> activeMatches = new ConcurrentHashMap<>();
    private static final int MAX_ACTIVE_MATCHES = 5;

    @Autowired
    private KafkaTemplate<String, MatchUpdateDTO> kafkaTemplate;

    @Scheduled(fixedRate = 30000) // Каждые 30 секунд (быстрее для демо)
    public void fetchAndPublishOdds() {
        log.info("Scheduler job started: Updating mock odds data...");

        // Если матчей мало, создаем новый
        if (activeMatches.size() < MAX_ACTIVE_MATCHES) {
            MatchUpdateDTO newMatch = createNewMatch();
            activeMatches.put(newMatch.externalId(), newMatch);
            sendUpdate(newMatch);
        }

        // Обновляем существующие матчи
        for (String externalId : activeMatches.keySet()) {
            MatchUpdateDTO currentMatch = activeMatches.get(externalId);
            MatchUpdateDTO updatedMatch = updateMatch(currentMatch);
            
            // Если матч завершен, удаляем из активных после отправки
            if ("finished".equals(updatedMatch.status())) {
                activeMatches.remove(externalId);
            } else {
                activeMatches.put(externalId, updatedMatch);
            }
            
            sendUpdate(updatedMatch);
        }

        log.info("Scheduler job finished. Active matches: {}", activeMatches.size());
    }

    private void sendUpdate(MatchUpdateDTO match) {
        try {
            log.info("Publishing update for match {} ({}) - Status: {}, Score: {}:{}", 
                    match.externalId(), match.homeTeam() + " vs " + match.awayTeam(), 
                    match.status(), match.homeScore(), match.awayScore());
            kafkaTemplate.send(TOPIC_EVENTS_UPDATE, match.externalId(), match);
        } catch (Exception e) {
            log.error("Failed to send match update for {}", match.externalId(), e);
        }
    }

    private MatchUpdateDTO createNewMatch() {
        String[] teams = {"Real Madrid", "Barcelona", "Manchester City", "Liverpool", "Bayern Munich", "PSG", "Juventus", "Inter"};
        String homeTeam = teams[random.nextInt(teams.length)];
        String awayTeam;
        do {
            awayTeam = teams[random.nextInt(teams.length)];
        } while (homeTeam.equals(awayTeam));

        String externalId = UUID.randomUUID().toString();
        // Начинаем как upcoming
        String status = "upcoming";
        
        return new MatchUpdateDTO(
            externalId, 
            status, 
            homeTeam, 
            awayTeam, 
            "0", 
            "0", 
            generateMarkets(homeTeam, awayTeam)
        );
    }

    private MatchUpdateDTO updateMatch(MatchUpdateDTO match) {
        String status = match.status();
        int homeScore = Integer.parseInt(match.homeScore());
        int awayScore = Integer.parseInt(match.awayScore());

        // Логика перехода статусов: upcoming -> live -> finished
        if ("upcoming".equals(status)) {
            // 30% шанс перехода в live
            if (random.nextDouble() < 0.3) {
                status = "live";
            }
        } else if ("live".equals(status)) {
            // Симуляция голов
            if (random.nextDouble() < 0.2) { // 20% шанс гола
                if (random.nextBoolean()) {
                    homeScore++;
                } else {
                    awayScore++;
                }
            }
            
            // 10% шанс завершения матча
            if (random.nextDouble() < 0.1) {
                status = "finished";
            }
        }

        return new MatchUpdateDTO(
            match.externalId(),
            status,
            match.homeTeam(),
            match.awayTeam(),
            String.valueOf(homeScore),
            String.valueOf(awayScore),
            generateMarkets(match.homeTeam(), match.awayTeam()) // Обновляем кэфы
        );
    }

    private List<MarketDTO> generateMarkets(String homeTeam, String awayTeam) {
        return Arrays.asList(
                new MarketDTO("Match Winner", Arrays.asList(
                        new OutcomeDTO(homeTeam, BigDecimal.valueOf(1.5 + random.nextDouble() * 2).setScale(2, BigDecimal.ROUND_HALF_UP)),
                        new OutcomeDTO(awayTeam, BigDecimal.valueOf(2.0 + random.nextDouble() * 2).setScale(2, BigDecimal.ROUND_HALF_UP)),
                        new OutcomeDTO("Draw", BigDecimal.valueOf(3.0 + random.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP))
                )),
                new MarketDTO("Total Goals", Arrays.asList(
                        new OutcomeDTO("Over 2.5", BigDecimal.valueOf(1.8 + random.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP)),
                        new OutcomeDTO("Under 2.5", BigDecimal.valueOf(1.9 + random.nextDouble()).setScale(2, BigDecimal.ROUND_HALF_UP))
                ))
        );
    }
}
