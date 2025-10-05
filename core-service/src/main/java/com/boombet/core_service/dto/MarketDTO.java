package com.boombet.core_service.dto;
import java.util.List;

public record MarketDTO(String name, List<OutcomeDTO> outcomes) {}
