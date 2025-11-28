package com.boombet.core_service.controller;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.boombet.core_service.dto.EventFilterRequest;
import com.boombet.core_service.dto.EventResponse;
import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Market;
import com.boombet.core_service.service.EventService;

@RestController
@RequestMapping("/api/v1/events")
public class EventController {
    @Autowired
    private EventService eventService;

    @GetMapping("/upcoming")
    public ResponseEntity<List<Event>> getUpcomingEvents() {
        List<Event> events = eventService.getUpcomingEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/live")
    public ResponseEntity<Page<EventResponse>> getLiveEvents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EventResponse> events = eventService.getEventsByStatus("live", page, size);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}/markets")
    public ResponseEntity<List<Market>> getMarketsForEvent(@PathVariable Long eventId) {
        List<Market> markets = eventService.getMarketsByEventId(eventId);
        return ResponseEntity.ok(markets);
    }

    /**
     * Фильтрация событий
     */
    @PostMapping("/filter")
    public ResponseEntity<Page<EventResponse>> filterEvents(@RequestBody EventFilterRequest filter) {
        Page<EventResponse> events = eventService.filterEvents(filter);
        return ResponseEntity.ok(events);
    }

    /**
     * Получить события по спорту
     */
    @GetMapping("/sport/{sportId}")
    public ResponseEntity<Page<EventResponse>> getEventsBySport(
            @PathVariable Integer sportId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EventResponse> events = eventService.getEventsBySport(sportId, page, size);
        return ResponseEntity.ok(events);
    }

    /**
     * Получить события по статусу
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<EventResponse>> getEventsByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EventResponse> events = eventService.getEventsByStatus(status, page, size);
        return ResponseEntity.ok(events);
    }

    /**
     * Поиск событий по названию команд
     */
    @GetMapping("/search")
    public ResponseEntity<Page<EventResponse>> searchEvents(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EventResponse> events = eventService.searchEvents(query, page, size);
        return ResponseEntity.ok(events);
    }

    /**
     * Получить события за период
     */
    @GetMapping("/range")
    public ResponseEntity<Page<EventResponse>> getEventsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<EventResponse> events = eventService.getEventsByDateRange(startDate, endDate, page, size);
        return ResponseEntity.ok(events);
    }
}
