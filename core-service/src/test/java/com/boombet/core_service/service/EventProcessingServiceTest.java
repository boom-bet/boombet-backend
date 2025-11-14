package com.boombet.core_service.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.model.Event;
import com.boombet.core_service.repository.EventRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class EventProcessingServiceTest {

    @Mock
    private EventService eventService;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private BetService betService;

    @InjectMocks
    private EventProcessingService eventProcessingService;

    private MatchUpdateDTO baseDto;

    @BeforeEach
    void setUp() {
        baseDto = new MatchUpdateDTO("ext-42", "finished", "Home", "Away", "1", "0", Collections.emptyList());
    }

    @Test
    void processMatchUpdate_triggersSettlementWhenFinished() {
        Event event = new Event();
        event.setExternalId("ext-42");
        event.setStatus("finished");

        when(eventRepository.findByExternalId("ext-42")).thenReturn(Optional.of(event));

        eventProcessingService.processMatchUpdate(baseDto);

        verify(eventService).createOrUpdateEvent(baseDto);
        verify(betService).settleBetsForEvent(event);
    }

    @Test
    void processMatchUpdate_skipsSettlementWhenNotFinished() {
        MatchUpdateDTO liveDto = new MatchUpdateDTO("ext-live", "live", "Home", "Away", null, null, Collections.emptyList());
        Event event = new Event();
        event.setExternalId("ext-live");
        event.setStatus("live");

        when(eventRepository.findByExternalId("ext-live")).thenReturn(Optional.of(event));

        eventProcessingService.processMatchUpdate(liveDto);

        verify(eventService).createOrUpdateEvent(liveDto);
        verifyNoInteractions(betService);
    }
}
