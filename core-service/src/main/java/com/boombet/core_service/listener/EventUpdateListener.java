package com.boombet.core_service.listener;

import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.service.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class EventUpdateListener {

    private static final Logger log = LoggerFactory.getLogger(EventUpdateListener.class);

    @Autowired
    private EventService eventService;

    @KafkaListener(topics = "events-updates", containerFactory = "kafkaListenerContainerFactory")
    public void handleEventUpdate(MatchUpdateDTO matchUpdate) {
        log.info("Received event update from Kafka: {}", matchUpdate.externalId());
        try {
            eventService.createOrUpdateEvent(matchUpdate);
        } catch (Exception e) {
            log.error("Error processing event update for externalId {}: {}", matchUpdate.externalId(), e.getMessage(), e);
        }
    }
}
