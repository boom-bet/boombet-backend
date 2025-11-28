package com.boombet.core_service.dto;

import com.boombet.core_service.model.Event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EventResponse {
    private Long eventId;
    private String sportName;
    private Integer sportId;
    private String teamA;
    private String teamB;
    private OffsetDateTime startTime;
    private String status;
    private String result;

    public static EventResponse fromEvent(Event event) {
        EventResponse response = new EventResponse();
        response.setEventId(event.getEventId());
        response.setSportName(event.getSport().getName());
        response.setSportId(event.getSport().getSportId());
        response.setTeamA(event.getTeamA());
        response.setTeamB(event.getTeamB());
        response.setStartTime(event.getStartTime());
        response.setStatus(event.getStatus());
        response.setResult(event.getResult());
        return response;
    }
}

