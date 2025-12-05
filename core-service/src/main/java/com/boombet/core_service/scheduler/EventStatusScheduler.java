package com.boombet.core_service.scheduler;

import java.time.OffsetDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.boombet.core_service.model.Event;
import com.boombet.core_service.repository.EventRepository;

@Component
public class EventStatusScheduler {

    private static final Logger log = LoggerFactory.getLogger(EventStatusScheduler.class);

    @Autowired
    private EventRepository eventRepository;

    /**
     * Каждые 30 секунд проверяет upcoming события и переводит их в live если время наступило
     */
    @Scheduled(fixedRate = 30000)
    @Transactional
    public void updateEventStatuses() {
        log.debug("Checking for upcoming events that should be live...");
        
        List<Event> upcomingEvents = eventRepository.findAllByStatus("upcoming");
        OffsetDateTime now = OffsetDateTime.now();
        int updatedCount = 0;

        for (Event event : upcomingEvents) {
            if (event.getStartTime() != null && event.getStartTime().isBefore(now)) {
                log.info("Moving event {} from upcoming to live (scheduled start: {})", 
                         event.getEventId(), event.getStartTime());
                event.setStatus("live");
                eventRepository.save(event);
                updatedCount++;
            }
        }

        if (updatedCount > 0) {
            log.info("Updated {} events from upcoming to live", updatedCount);
        }
    }
}
