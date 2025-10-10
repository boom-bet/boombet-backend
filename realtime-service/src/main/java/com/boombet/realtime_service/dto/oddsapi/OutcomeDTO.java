package com.boombet.realtime_service.dto.oddsapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OutcomeDTO(
    String name,
    double price
) {}
