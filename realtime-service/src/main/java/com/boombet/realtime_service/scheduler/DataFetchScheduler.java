package com.boombet.realtime_service.scheduler;

import com.boombet.realtime_service.dto.MatchUpdateDTO;
import com.boombet.realtime_service.handler.UpdateHandler;
import com.boombet.realtime_service.parser.SportsDataParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataFetchScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataFetchScheduler.class);
    public static final String TOPIC_EVENTS_UPDATE = "events-updates";

    @Autowired private SportsDataParser parser;
    @Autowired private UpdateHandler updateHandler;
    @Autowired private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, MatchUpdateDTO> kafkaTemplate;

    @Scheduled(fixedRate = 60000)
    public void fetchAndProcessMatches() {
        log.info("Scheduler job started: Fetching live scores...");
        
        List<MatchUpdateDTO> matches = parser.fetchLiveMatches();

        if (!matches.isEmpty()) {
            log.info("Publishing {} match updates to Kafka topic '{}'...", matches.size(), TOPIC_EVENTS_UPDATE);
            
            for (MatchUpdateDTO match : matches) {
                kafkaTemplate.send(TOPIC_EVENTS_UPDATE, match.externalId(), match);
            }
            
            try {
                String websocketMessage = objectMapper.writeValueAsString(matches);
                updateHandler.broadcast(websocketMessage);
            } catch (Exception e) {
                log.error("Could not serialize matches to JSON for WebSocket", e);
            }
        }
        
        log.info("Scheduler job finished.");
    }
}
