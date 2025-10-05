package com.boombet.core_service.listener;

import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.service.EventProcessingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger log = LoggerFactory.getLogger(KafkaConsumerService.class);

    @Autowired
    private EventProcessingService eventProcessingService;

    @KafkaListener(topics = "events-updates", groupId = "core-service-group")
    public void handleMatchUpdate(MatchUpdateDTO matchUpdate) {
        log.info("<<<< Received Match Update from Kafka for externalId: {}", matchUpdate.externalId());
        try {
            eventProcessingService.processMatchUpdate(matchUpdate);
        } catch (Exception e) {
            log.error("Failed to process match update for externalId {}: {}", matchUpdate.externalId(), e.getMessage(), e);
        }
    }
}
