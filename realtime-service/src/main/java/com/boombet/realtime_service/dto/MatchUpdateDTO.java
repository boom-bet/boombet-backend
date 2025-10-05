package com.boombet.realtime_service.dto;

import java.util.List;

public record MatchUpdateDTO(
    String externalId,
    String status,
    String homeTeam,
    String awayTeam,
    String homeScore,
    String awayScore,
    List<MarketDTO> markets
) {}
