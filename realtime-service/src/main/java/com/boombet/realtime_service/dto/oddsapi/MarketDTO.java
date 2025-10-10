package com.boombet.realtime_service.dto.oddsapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MarketDTO(
    String key,
    List<OutcomeDTO> outcomes
) {}
