package com.boombet.core_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.boombet.core_service.dto.MarketDTO;
import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.dto.OutcomeDTO;
import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Market;
import com.boombet.core_service.model.Sport;
import com.boombet.core_service.repository.EventRepository;
import com.boombet.core_service.repository.MarketRepository;
import com.boombet.core_service.repository.OutcomeRepository;
import com.boombet.core_service.repository.SportRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private MarketRepository marketRepository;
    @Mock
    private OutcomeRepository outcomeRepository;
    @Mock
    private SportRepository sportRepository;

    @InjectMocks
    private EventService eventService;

    @Test
    void createOrUpdateEvent_createsSportMarketAndOutcome() {
        MatchUpdateDTO dto = new MatchUpdateDTO(
                "ext-1",
                "finished",
                "Home FC",
                "Away FC",
                "2",
                "1",
                List.of(new MarketDTO(
                        "Match Winner",
                        List.of(new OutcomeDTO("Home FC", new BigDecimal("1.90")))
                ))
        );

        when(sportRepository.findByName("soccer_mock")).thenReturn(Optional.empty());
        when(sportRepository.save(any(Sport.class))).thenAnswer(invocation -> {
            Sport sport = invocation.getArgument(0);
            sport.setSportId(1);
            return sport;
        });
        when(eventRepository.findByExternalId("ext-1")).thenReturn(Optional.empty());
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> {
            Event event = invocation.getArgument(0);
            event.setEventId(10L);
            return event;
        });
        when(marketRepository.findByNameAndEvent_EventId("Match Winner", 10L)).thenReturn(Optional.empty());
        when(marketRepository.save(any(Market.class))).thenAnswer(invocation -> {
            Market market = invocation.getArgument(0);
            market.setMarketId(7L);
            return market;
        });
        when(outcomeRepository.findByMarket_MarketIdAndName(7L, "Home FC")).thenReturn(Optional.empty());

        eventService.createOrUpdateEvent(dto);

        ArgumentCaptor<Event> eventCaptor = ArgumentCaptor.forClass(Event.class);
        verify(eventRepository, atLeastOnce()).save(eventCaptor.capture());
        Event savedEvent = eventCaptor.getValue();
        assertThat(savedEvent.getTeamA()).isEqualTo("Home FC");
        assertThat(savedEvent.getResult()).isEqualTo("2-1");

        verify(marketRepository).save(argThat(market ->
                market.getName().equals("Match Winner") && market.getEvent().getEventId().equals(10L)
        ));

        verify(outcomeRepository).save(argThat(outcome ->
                outcome.getName().equals("Home FC")
                        && outcome.getCurrentOdds().compareTo(new BigDecimal("1.90")) == 0
                        && outcome.isActive()
        ));
    }
}
