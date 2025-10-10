package com.boombet.core_service.service;

import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Market;
import com.boombet.core_service.model.Outcome;
import com.boombet.core_service.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EventProcessingService {
    @Autowired private EventRepository eventRepository;
    @Autowired private MarketRepository marketRepository;
    @Autowired private OutcomeRepository outcomeRepository;
    @Autowired private BetService betService;
    @Autowired private EventFactory eventFactory;

    @Transactional
    public void processMatchUpdate(MatchUpdateDTO dto) {
        Event event = eventRepository.findByExternalId(dto.externalId()).orElseGet(() -> eventFactory.createEvent(dto));

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
