package com.boombet.realtime_service.dto.oddsapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record BookmakerDTO(
    String key,
    String title,
    List<MarketDTO> markets
) {}
