package com.boombet.core_service.dto;

import lombok.Data;

@Data
public class EventFilterRequest {
    private Integer sportId;
    private String status;
    private String startDate; // ISO format
    private String endDate;   // ISO format
    private String query;     // Search query for team names
    private Integer page = 0;
    private Integer size = 20;
}

