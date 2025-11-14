package com.boombet.core_service.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.boombet.core_service.config.KafkaTopicConfig;
import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.service.EventProcessingService;

@Service
public class MatchUpdateListener {

    private static final Logger log = LoggerFactory.getLogger(MatchUpdateListener.class);

    @Autowired
    private EventProcessingService eventProcessingService;

    @KafkaListener(
            topics = KafkaTopicConfig.TOPIC_EVENTS_UPDATE,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleMatchUpdate(MatchUpdateDTO matchUpdate) {
        log.info("<<<< Received match update from Kafka: {}", matchUpdate.externalId());
        try {
            eventProcessingService.processMatchUpdate(matchUpdate);
        } catch (Exception e) {
            log.error("Failed to process match update {}: {}", matchUpdate.externalId(), e.getMessage(), e);
        }
    }
}
