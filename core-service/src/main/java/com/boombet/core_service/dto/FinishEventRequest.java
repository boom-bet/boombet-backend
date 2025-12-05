package com.boombet.core_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FinishEventRequest {
    private String result; // например "2:1"
}
