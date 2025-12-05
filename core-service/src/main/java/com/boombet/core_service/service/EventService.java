package com.boombet.core_service.service;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.boombet.core_service.dto.EventFilterRequest;
import com.boombet.core_service.dto.EventResponse;
import com.boombet.core_service.dto.MarketDTO;
import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.dto.OutcomeDTO;
import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Market;
import com.boombet.core_service.model.Outcome;
import com.boombet.core_service.model.Sport;
import com.boombet.core_service.repository.EventRepository;
import com.boombet.core_service.repository.MarketRepository;
import com.boombet.core_service.repository.OutcomeRepository;
import com.boombet.core_service.repository.SportRepository;

import jakarta.transaction.Transactional;

@Service
public class EventService {
    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private OutcomeRepository outcomeRepository;

    @Autowired
    private SportRepository sportRepository;

    public List<Event> getUpcomingEvents() {
        return eventRepository.findUpcomingEvents(OffsetDateTime.now());
    }

    public List<Market> getMarketsByEventId(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new RuntimeException("Event not found with id: " + eventId);
        }
        return marketRepository.findAllByEvent_EventId(eventId);
    }

    @Transactional
    public void createOrUpdateEvent(MatchUpdateDTO dto) {
        Sport sport = sportRepository.findByName("soccer_mock").orElseGet(() -> {
            Sport newSport = new Sport();
            newSport.setName("soccer_mock");
            return sportRepository.save(newSport);
        });

        Event event = eventRepository.findByExternalId(dto.externalId()).orElse(new Event());
        event.setExternalId(dto.externalId());
        event.setTeamA(dto.homeTeam());
        event.setTeamB(dto.awayTeam());
        
        // Автоматический переход upcoming -> live если время события наступило
        if ("upcoming".equals(event.getStatus()) && event.getStartTime() != null 
                && event.getStartTime().isBefore(OffsetDateTime.now())) {
            event.setStatus("live");
        } else {
            event.setStatus(dto.status());
        }
        
        if (hasScore(dto)) {
            event.setResult(dto.homeScore().trim() + "-" + dto.awayScore().trim());
        }
        event.setSport(sport);
        if (event.getStartTime() == null) {
            event.setStartTime(OffsetDateTime.now().plusHours(1));
        }
        Event savedEvent = eventRepository.save(event);

        if (dto.markets() == null) {
            return;
        }

        for (MarketDTO marketDTO : dto.markets()) {
            Market market = marketRepository.findByNameAndEvent_EventId(marketDTO.name(), savedEvent.getEventId()).orElse(new Market());
            market.setEvent(savedEvent);
            market.setName(marketDTO.name());
            Market savedMarket = marketRepository.save(market);

            if (marketDTO.outcomes() == null) {
                continue;
            }

            for (OutcomeDTO outcomeDTO : marketDTO.outcomes()) {
                Outcome outcome = outcomeRepository.findByMarket_MarketIdAndName(savedMarket.getMarketId(), outcomeDTO.name()).orElse(new Outcome());
                outcome.setMarket(savedMarket);
                outcome.setName(outcomeDTO.name());
                outcome.setCurrentOdds(outcomeDTO.price());
                outcome.setActive(true);
                outcomeRepository.save(outcome);
            }
        }
    }

    private boolean hasScore(MatchUpdateDTO dto) {
        return dto.homeScore() != null && dto.awayScore() != null
                && !dto.homeScore().isBlank() && !dto.awayScore().isBlank();
    }

    /**
     * Фильтрация событий с пагинацией
     */
    public Page<EventResponse> filterEvents(EventFilterRequest filter) {
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize());

        // Парсинг дат если заданы
        OffsetDateTime startDate = null;
        OffsetDateTime endDate = null;

        if (filter.getStartDate() != null && !filter.getStartDate().isEmpty()) {
            try {
                startDate = OffsetDateTime.parse(filter.getStartDate());
            } catch (Exception e) {
                // Игнорируем невалидные даты
            }
        }

        if (filter.getEndDate() != null && !filter.getEndDate().isEmpty()) {
            try {
                endDate = OffsetDateTime.parse(filter.getEndDate());
            } catch (Exception e) {
                // Игнорируем невалидные даты
            }
        }

        // Применяем фильтры
        Page<Event> events = eventRepository.findByFilters(
            filter.getSportId(),
            filter.getStatus(),
            startDate,
            endDate,
            filter.getQuery(),
            pageable
        );

        return events.map(EventResponse::fromEvent);
    }

    /**
     * Получить события по спорту
     */
    public Page<EventResponse> getEventsBySport(Integer sportId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.findBySportSportIdOrderByStartTimeAsc(sportId, pageable);
        return events.map(EventResponse::fromEvent);
    }

    /**
     * Получить события по статусу
     */
    public Page<EventResponse> getEventsByStatus(String status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.findByStatusOrderByStartTimeAsc(status, pageable);
        return events.map(EventResponse::fromEvent);
    }

    /**
     * Поиск событий по названию команд
     */
    public Page<EventResponse> searchEvents(String query, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.searchByTeams(query, pageable);
        return events.map(EventResponse::fromEvent);
    }

    /**
     * Получить события за период
     */
    public Page<EventResponse> getEventsByDateRange(OffsetDateTime startDate, OffsetDateTime endDate, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Event> events = eventRepository.findByDateRange(startDate, endDate, pageable);
        return events.map(EventResponse::fromEvent);
    }

    /**
     * Завершить событие с результатом
     */
    @Transactional
    public void finishEvent(Long eventId, String result) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId));
        
        event.setStatus("finished");
        event.setResult(result);
        eventRepository.save(event);
    }
}

