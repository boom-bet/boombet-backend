package com.boombet.core_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_EVENTS_UPDATE = "events-updates";
    
    public static final String TOPIC_NOTIFICATIONS = "betting-notifications";

    @Bean
    public NewTopic eventsUpdateTopic() {
        return TopicBuilder.name(TOPIC_EVENTS_UPDATE)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(TOPIC_NOTIFICATIONS)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
