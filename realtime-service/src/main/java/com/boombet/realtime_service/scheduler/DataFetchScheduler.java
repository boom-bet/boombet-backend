package com.boombet.realtime_service.scheduler;

import com.boombet.realtime_service.dto.MarketDTO;
import com.boombet.realtime_service.dto.MatchUpdateDTO;
import com.boombet.realtime_service.dto.OutcomeDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Component
public class DataFetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataFetchScheduler.class);
    public static final String TOPIC_EVENTS_UPDATE = "events-updates";
    private final Random random = new Random();

    @Autowired
    private KafkaTemplate<String, MatchUpdateDTO> kafkaTemplate;

    @Scheduled(fixedRate = 15000) // 15 seconds
    public void fetchAndPublishOdds() {
        log.info("Scheduler job started: Generating mock odds data...");

        MatchUpdateDTO mockData = generateMockMatchUpdate();
        log.info("Publishing mock match update to Kafka for externalId {}", mockData.externalId());

        try {
            kafkaTemplate.send(TOPIC_EVENTS_UPDATE, mockData.externalId(), mockData);
        } catch (Exception e) {
            log.error("Failed to send mock match update to Kafka for externalId {}", mockData.externalId(), e);
        }

        log.info("Scheduler job finished.");
    }

    private MatchUpdateDTO generateMockMatchUpdate() {
        String[] teams = {"Team A", "Team B", "Team C", "Team D", "Team E", "Team F"};
        String homeTeam = teams[random.nextInt(teams.length)];
        String awayTeam;
        do {
            awayTeam = teams[random.nextInt(teams.length)];
        } while (homeTeam.equals(awayTeam));

        String externalId = UUID.randomUUID().toString();
        String status = random.nextBoolean() ? "live" : "upcoming";
        int homeScore = status.equals("live") ? random.nextInt(5) : 0;
        int awayScore = status.equals("live") ? random.nextInt(5) : 0;

        List<MarketDTO> markets = Arrays.asList(
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

        return new MatchUpdateDTO(externalId, status, homeTeam, awayTeam, String.valueOf(homeScore), String.valueOf(awayScore), markets);
    }
}
