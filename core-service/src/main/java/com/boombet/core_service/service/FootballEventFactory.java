package com.boombet.core_service.service;

import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.model.Event;
import com.boombet.core_service.repository.SportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("footballEventFactory")
public class FootballEventFactory implements EventFactory {
    @Autowired private SportRepository sportRepository;
    
    @Override
    public Event createEvent(MatchUpdateDTO dto) {
        Event event = new Event();
        event.setExternalId(dto.externalId());
        event.setTeamA(dto.homeTeam());
        event.setTeamB(dto.awayTeam());
        event.setSport(sportRepository.findById(1).orElseThrow(() -> new RuntimeException("Sport 'Football' not found")));
        return event;
    }
}
