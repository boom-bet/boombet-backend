package com.boombet.core_service.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.repository.EventRepository;

import jakarta.transaction.Transactional;

@Service
public class EventProcessingService {

	private static final Logger log = LoggerFactory.getLogger(EventProcessingService.class);

	private final EventService eventService;
	private final EventRepository eventRepository;
	private final BetService betService;

	public EventProcessingService(EventService eventService,
								  EventRepository eventRepository,
								  BetService betService) {
		this.eventService = eventService;
		this.eventRepository = eventRepository;
		this.betService = betService;
	}

	@Transactional
	public void processMatchUpdate(MatchUpdateDTO matchUpdate) {
		log.info("Processing match update for externalId {}", matchUpdate.externalId());
		eventService.createOrUpdateEvent(matchUpdate);

		eventRepository.findByExternalId(matchUpdate.externalId())
				.ifPresent(event -> {
					if (shouldSettle(matchUpdate.status())) {
						log.info("Event {} marked as finished. Triggering bet settlement.", event.getExternalId());
						betService.settleBetsForEvent(event);
					}
				});
	}

	private boolean shouldSettle(String status) {
		return status != null && status.equalsIgnoreCase("finished");
	}
}
