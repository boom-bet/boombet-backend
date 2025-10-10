package com.boombet.realtime_service.dto.oddsapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OddsApiResponseDTO(
    String id,
    @JsonProperty("sport_key") String sportKey,
    @JsonProperty("sport_title") String sportTitle,
    @JsonProperty("commence_time") String commenceTime,
    @JsonProperty("home_team") String homeTeam,
    @JsonProperty("away_team") String awayTeam,
    List<BookmakerDTO> bookmakers
) {}
