package com.boombet.core_service.service;
import com.boombet.core_service.dto.MatchUpdateDTO;
import com.boombet.core_service.model.Event;

public interface EventFactory {
    Event createEvent(MatchUpdateDTO dto);
}
