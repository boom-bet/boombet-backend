package com.boombet.realtime_service.dto;

import java.util.List;

public record MarketDTO(String name, List<OutcomeDTO> outcomes) {}
