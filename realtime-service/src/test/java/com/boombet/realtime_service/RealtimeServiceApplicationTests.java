package com.boombet.realtime_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class RealtimeServiceApplicationTests {

	@MockBean
	private KafkaTemplate<String, ?> kafkaTemplate;

	@MockBean
	private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

	@Test
	void contextLoads() {
	}

}
