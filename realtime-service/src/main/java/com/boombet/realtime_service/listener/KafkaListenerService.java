package com.boombet.realtime_service.listener;

import com.boombet.realtime_service.handler.UpdateHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class KafkaListenerService {

    @Autowired
    private UpdateHandler updateHandler;

    @KafkaListener(topics = "betting-notifications", groupId = "realtime-group")
    public void handleNotification(String message) {
        updateHandler.broadcast(message);
    }
}
