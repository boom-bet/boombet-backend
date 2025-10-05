package com.boombet.core_service.service;

import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Market;
import com.boombet.core_service.model.Outcome;
import com.boombet.core_service.repository.*;
import com.boombet.core_service.service.BetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventProcessingService {
    @Autowired private EventRepository eventRepository;
    @Autowired private MarketRepository marketRepository;
    @Autowired private OutcomeRepository outcomeRepository;
    @Autowired private SportRepository sportRepository;
    @Autowired private BetService betService;

    @Transactional
    public void processMatchUpdate(MatchUpdateDTO dto) {
        Event event = eventRepository.findByExternalId(dto.externalId())
                .orElseGet(() -> createNewEvent(dto));

        String internalStatus = mapExternalStatus(dto.status());
        event.setStatus(internalStatus);
        event.setResult(String.format("%s-%s", dto.homeScore(), dto.awayScore()));
        eventRepository.save(event);

        dto.markets().forEach(marketDTO -> {
            Market market = marketRepository.findByNameAndEvent_EventId(marketDTO.name(), event.getEventId())
                    .orElseGet(() -> {
                        Market newMarket = new Market();
                        newMarket.setName(marketDTO.name());
                        newMarket.setEvent(event);
                        return marketRepository.save(newMarket);
                    });

            marketDTO.outcomes().forEach(outcomeDTO -> {
                Outcome outcome = outcomeRepository.findByNameAndMarket_MarketId(outcomeDTO.name(), market.getMarketId())
                        .orElseGet(() -> {
                            Outcome newOutcome = new Outcome();
                            newOutcome.setName(outcomeDTO.name());
                            newOutcome.setMarket(market);
                            newOutcome.setActive(true);
                            return newOutcome;
                        });
                outcome.setCurrentOdds(outcomeDTO.odds());
                outcomeRepository.save(outcome);
            });
        });
        eventRepository.save(event);
        if ("finished".equals(internalStatus)) {
            betService.settleBetsForEvent(event);
        }
    }

    private Event createNewEvent(MatchUpdateDTO dto) {
        Event event = new Event();
        event.setExternalId(dto.externalId());
        event.setTeamA(dto.homeTeam());
        event.setTeamB(dto.awayTeam());
        
        event.setSport(sportRepository.findById(1).orElseThrow()); 
        
        return event;
    }

    private String mapExternalStatus(String externalStatus) {
        if (externalStatus == null || externalStatus.isEmpty()) {
            return "upcoming";
        }
        
        String lowerCaseStatus = externalStatus.toLowerCase();
        
        if (lowerCaseStatus.contains("завершен")) {
            return "finished";
        }
        if (lowerCaseStatus.contains("перенесен")) {
            return "cancelled";
        }
        if (lowerCaseStatus.matches(".*\\d+.*")) {
            return "live";
        }

        return "upcoming";
    }
}
