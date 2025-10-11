package com.boombet.realtime_service.adapter; // Убедитесь, что имя пакета ваше

import com.boombet.realtime_service.dto.MarketDTO;
import com.boombet.realtime_service.dto.MatchUpdateDTO;
import com.boombet.realtime_service.dto.OutcomeDTO;
import com.boombet.realtime_service.dto.oddsapi.BookmakerDTO;
import com.boombet.realtime_service.dto.oddsapi.OddsApiResponseDTO;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OddsAdapter {

    public List<MatchUpdateDTO> toMatchUpdateDTOs(OddsApiResponseDTO[] apiResponse) {
        if (apiResponse == null || apiResponse.length == 0) {
            return Collections.emptyList();
        }

        List<MatchUpdateDTO> updates = new ArrayList<>();
        for (OddsApiResponseDTO rawMatch : apiResponse) {
            updates.add(adaptSingleMatch(rawMatch));
        }
        return updates;
    }

    private MatchUpdateDTO adaptSingleMatch(OddsApiResponseDTO rawMatch) {
        List<MarketDTO> markets = new ArrayList<>();
        
        if (rawMatch.bookmakers() != null && !rawMatch.bookmakers().isEmpty()) {
            BookmakerDTO bookmaker = rawMatch.bookmakers().get(0);
            
            markets = bookmaker.markets().stream()
                .map(apiMarket -> new MarketDTO(
                    mapMarketName(apiMarket.key()),
                    apiMarket.outcomes().stream()
                        .map(apiOutcome -> new OutcomeDTO(
                            mapOutcomeName(rawMatch, apiOutcome.name()), 
                            BigDecimal.valueOf(apiOutcome.price())
                        )).collect(Collectors.toList())
                )).collect(Collectors.toList());
        }
        
        OffsetDateTime startTime = OffsetDateTime.parse(rawMatch.commenceTime());

        return new MatchUpdateDTO(
            rawMatch.id(),
            startTime.toString(),
            rawMatch.homeTeam(),
            rawMatch.awayTeam(),
            "-",
            "-",
            markets
        );
    }

    private String mapMarketName(String apiKey) {
        if ("h2h".equalsIgnoreCase(apiKey)) {
            return "Исход матча";
        }
        return apiKey;
    }

    private String mapOutcomeName(OddsApiResponseDTO rawMatch, String apiOutcomeName) {
        if (apiOutcomeName.equalsIgnoreCase(rawMatch.homeTeam())) {
            return "1";
        }
        if (apiOutcomeName.equalsIgnoreCase(rawMatch.awayTeam())) {
            return "2";
        }
        return "X";
    }
}
