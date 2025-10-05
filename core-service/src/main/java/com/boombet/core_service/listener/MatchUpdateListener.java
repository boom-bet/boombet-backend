package com.boombet.core_service.listener;

import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.service.EventProcessingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class MatchUpdateListener {

    private static final Logger log = LoggerFactory.getLogger(MatchUpdateListener.class);

    @Autowired
    private EventProcessingService eventProcessingService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(topics = "events-updates", groupId = "core-service-group")
    public void handleMatchUpdate(String messageJson) { // Принимаем String
        log.info("<<<< Received Raw JSON from Kafka: {}", messageJson);
        try {
            MatchUpdateDTO matchUpdate = objectMapper.readValue(messageJson, MatchUpdateDTO.class);
            
            eventProcessingService.processMatchUpdate(matchUpdate);
        } catch (Exception e) {
            log.error("Failed to deserialize or process message: {}", e.getMessage(), e);
        }
    }
}
