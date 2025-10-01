package com.boombet.core_service.service;

import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Market;
import com.boombet.core_service.repository.EventRepository;
import com.boombet.core_service.repository.MarketRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MarketRepository marketRepository;

    public List<Event> getUpcomingEvents() {
        return eventRepository.findAllByStatus("upcoming");
    }

    public List<Market> getMarketsByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found with id: " + eventId);
        }
        return marketRepository.findAllByEvent_EventId(eventId);
    }
}
