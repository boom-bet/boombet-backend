package com.boombet.realtime_service.client; // Создайте пакет 'client'

import com.boombet.realtime_service.dto.oddsapi.OddsApiResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OddsApiClient {

    private static final Logger log = LoggerFactory.getLogger(OddsApiClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${the-odds-api.base-url}")
    private String baseUrl;

    @Value("${the-odds-api.api-key}")
    private String apiKey;

    public OddsApiResponseDTO[] fetchUpcomingFootballOdds() {
        String url = String.format(
            "%s/sports/soccer_epl/odds/?apiKey=%s&regions=eu&markets=h2h",
            baseUrl,
            apiKey
        );

        log.info("Fetching data from The Odds API: {}", url);
        try {
            ResponseEntity<OddsApiResponseDTO[]> response = restTemplate.getForEntity(url, OddsApiResponseDTO[].class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch data from The Odds API: {}", e.getMessage());
            return null;
        }
    }
}
