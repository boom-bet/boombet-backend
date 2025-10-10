package com.boombet.realtime_service.scheduler;

import com.boombet.realtime_service.adapter.OddsAdapter;
import com.boombet.realtime_service.client.OddsApiClient;
import com.boombet.realtime_service.dto.MatchUpdateDTO;
import com.boombet.realtime_service.dto.oddsapi.OddsApiResponseDTO;
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

    @Autowired private OddsApiClient apiClient;
    @Autowired private OddsAdapter adapter;
    @Autowired private KafkaTemplate<String, MatchUpdateDTO> kafkaTemplate;

    @Scheduled(fixedRate = 900000)
    public void fetchAndPublishOdds() {
        log.info("Scheduler job started: Fetching odds from The Odds API...");
        
        OddsApiResponseDTO[] response = apiClient.fetchUpcomingFootballOdds();

        if (response != null && response.length > 0) {
            List<MatchUpdateDTO> matchUpdates = adapter.toMatchUpdateDTOs(response);

            log.info("Publishing {} match updates to Kafka...", matchUpdates.size());
            
            matchUpdates.forEach(dto -> {
                try {
                    kafkaTemplate.send(TOPIC_EVENTS_UPDATE, dto.externalId(), dto);
                } catch (Exception e) {
                    log.error("Failed to send match update to Kafka for externalId {}", dto.externalId(), e);
                }
            });
        }
        log.info("Scheduler job finished.");
    }
}
