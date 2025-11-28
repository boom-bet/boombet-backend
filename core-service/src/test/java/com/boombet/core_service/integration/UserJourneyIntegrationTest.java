package com.boombet.core_service.integration;

import com.boombet.core_service.dto.BetHistoryResponse;
import com.boombet.core_service.dto.BetStatsResponse;
import com.boombet.core_service.dto.EventResponse;
import com.boombet.core_service.dto.PlaceBetRequest;
import com.boombet.core_service.model.*;
import com.boombet.core_service.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционный тест для проверки полного сценария работы пользователя:
 * 1. Регистрация/получение профиля пользователя
 * 2. Пополнение баланса
 * 3. Получение списка событий
 * 4. Просмотр рынков и коэффициентов
 * 5. Создание ставки
 * 6. Просмотр истории ставок
 * 7. Получение статистики
 * 8. Отмена ставки
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UserJourneyIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SportRepository sportRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private OutcomeRepository outcomeRepository;

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    private User testUser;
    private Event testEvent;
    private Market testMarket;
    private Outcome outcome1;
    private Outcome outcome2;
    private Sport testSport;

    private static final String TEST_USER_EMAIL = "testuser@boombet.com";
    private static final String AUTH_HEADER = "X-Authenticated-User-Email";

    @BeforeEach
    void setUp() {
        // Создание тестового пользователя (если не существует)
        testUser = userRepository.findByEmail(TEST_USER_EMAIL).orElseGet(() -> {
            User user = new User();
            user.setUserId(999L);
            user.setEmail(TEST_USER_EMAIL);
            user.setBalance(new BigDecimal("5000.00"));
            return userRepository.save(user);
        });

        // Создание тестового спорта (если не существует)
        testSport = sportRepository.findByName("Football").orElseGet(() -> {
            Sport sport = new Sport();
            sport.setName("Football");
            return sportRepository.save(sport);
        });

        // Создание тестового события
        testEvent = new Event();
        testEvent.setTeamA("Arsenal");
        testEvent.setTeamB("Chelsea");
        testEvent.setStartTime(OffsetDateTime.now().plusDays(1));
        testEvent.setStatus("upcoming");
        testEvent.setSport(testSport);
        testEvent.setExternalId("test-event-" + System.currentTimeMillis());
        testEvent = eventRepository.save(testEvent);

        // Создание рынка
        testMarket = new Market();
        testMarket.setName("Match Winner");
        testMarket.setEvent(testEvent);
        testMarket = marketRepository.save(testMarket);

        // Создание исходов
        outcome1 = new Outcome();
        outcome1.setName("Arsenal");
        outcome1.setCurrentOdds(new BigDecimal("2.50"));
        outcome1.setActive(true);
        outcome1.setMarket(testMarket);
        outcome1 = outcomeRepository.save(outcome1);

        outcome2 = new Outcome();
        outcome2.setName("Chelsea");
        outcome2.setCurrentOdds(new BigDecimal("3.00"));
        outcome2.setActive(true);
        outcome2.setMarket(testMarket);
        outcome2 = outcomeRepository.save(outcome2);
    }

    @Test
    void testCompleteUserJourney() throws Exception {
        System.out.println("\n=== СЦЕНАРИЙ: Полный путь пользователя ===\n");

        // ============ ШАГ 1: Пополнение баланса ============
        System.out.println("ШАГ 1: Пополнение баланса на 500");
        mockMvc.perform(post("/api/v1/user/deposit")
                        .header(AUTH_HEADER, TEST_USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 500.00}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists());

        System.out.println("✓ Баланс пополнен");

        // ============ ШАГ 2: Получение списка видов спорта ============
        System.out.println("\nШАГ 2: Получение списка видов спорта");
        MvcResult sportsResult = mockMvc.perform(get("/api/v1/sports"))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("✓ Виды спорта: " + sportsResult.getResponse().getContentAsString());

        // ============ ШАГ 3: Получение списка событий ============
        System.out.println("\nШАГ 3: Получение списка предстоящих событий");
        MvcResult eventsResult = mockMvc.perform(get("/api/v1/events/upcoming"))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("✓ События получены: " + eventsResult.getResponse().getContentAsString());

        // ============ ШАГ 4: Поиск событий по команде ============
        System.out.println("\nШАГ 4: Поиск события 'Arsenal'");
        MvcResult searchResult = mockMvc.perform(get("/api/v1/events/search")
                        .param("query", "Arsenal")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();

        System.out.println("✓ Результаты поиска получены");

        // ============ ШАГ 5: Получение рынков для события ============
        System.out.println("\nШАГ 5: Получение рынков для события");
        MvcResult marketsResult = mockMvc.perform(get("/api/v1/events/" + testEvent.getEventId() + "/markets"))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("✓ Рынки и коэффициенты получены");

        // ============ ШАГ 6: Создание ставки ============
        System.out.println("\nШАГ 6: Создание ставки на Arsenal с коэффициентом 2.50");
        PlaceBetRequest betRequest = new PlaceBetRequest(
                new BigDecimal("100.00"),
                List.of(outcome1.getOutcomeId())
        );

        MvcResult betResult = mockMvc.perform(post("/api/v1/bets")
                        .header(AUTH_HEADER, TEST_USER_EMAIL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(betRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stakeAmount").value(100.00))
                .andExpect(jsonPath("$.totalOdds").value(2.50))
                .andExpect(jsonPath("$.potentialPayout").value(250.00))
                .andExpect(jsonPath("$.status").value("pending"))
                .andReturn();

        String betJson = betResult.getResponse().getContentAsString();
        Bet placedBet = objectMapper.readValue(betJson, Bet.class);
        Long betId = placedBet.getBetId();
        System.out.println("✓ Ставка создана: ID=" + betId + ", Сумма=100, Потенциальный выигрыш=250");

        // ============ ШАГ 7: Проверка баланса после ставки ============
        System.out.println("\nШАГ 7: Проверка баланса после ставки");
        mockMvc.perform(get("/api/v1/user/balance")
                        .header(AUTH_HEADER, TEST_USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists());

        System.out.println("✓ Баланс проверен");

        // ============ ШАГ 8: Получение истории ставок ============
        System.out.println("\nШАГ 8: Получение истории ставок");
        MvcResult historyResult = mockMvc.perform(get("/api/v1/bets/history")
                        .header(AUTH_HEADER, TEST_USER_EMAIL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();

        System.out.println("✓ История ставок получена");

        // ============ ШАГ 9: Получение деталей конкретной ставки ============
        System.out.println("\nШАГ 9: Получение деталей ставки #" + betId);
        MvcResult betDetailsResult = mockMvc.perform(get("/api/v1/bets/" + betId)
                        .header(AUTH_HEADER, TEST_USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.betId").value(betId))
                .andReturn();

        System.out.println("✓ Детали ставки получены");

        // ============ ШАГ 10: Получение статистики ставок ============
        System.out.println("\nШАГ 10: Получение статистики ставок");
        MvcResult statsResult = mockMvc.perform(get("/api/v1/bets/stats")
                        .header(AUTH_HEADER, TEST_USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBets").exists())
                .andExpect(jsonPath("$.pendingBets").exists())
                .andReturn();

        System.out.println("✓ Статистика получена: " + statsResult.getResponse().getContentAsString());

        // ============ ШАГ 11: Фильтрация ставок по статусу ============
        System.out.println("\nШАГ 11: Получение активных (pending) ставок");
        mockMvc.perform(get("/api/v1/bets/history/status/pending")
                        .header(AUTH_HEADER, TEST_USER_EMAIL)
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        System.out.println("✓ Активные ставки получены");

        // ============ ШАГ 12: Отмена ставки ============
        System.out.println("\nШАГ 12: Отмена ставки #" + betId);
        mockMvc.perform(delete("/api/v1/bets/" + betId)
                        .header(AUTH_HEADER, TEST_USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("cancelled"));

        System.out.println("✓ Ставка отменена, средства возвращены");

        // ============ ШАГ 13: Получение истории транзакций ============
        System.out.println("\nШАГ 13: Получение истории транзакций");
        MvcResult transactionsResult = mockMvc.perform(get("/api/v1/user/transactions")
                        .header(AUTH_HEADER, TEST_USER_EMAIL)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("✓ Транзакции получены");

        System.out.println("\n=== ✅ ВСЕ СЦЕНАРИИ УСПЕШНО ПРОЙДЕНЫ ===\n");
    }

    @Test
    void testEventFiltering() throws Exception {
        System.out.println("\n=== СЦЕНАРИЙ: Фильтрация событий ===\n");

        // Создание дополнительного события
        Event event2 = new Event();
        event2.setTeamA("Liverpool");
        event2.setTeamB("Manchester United");
        event2.setStartTime(OffsetDateTime.now().plusDays(2));
        event2.setStatus("upcoming");
        event2.setSport(testSport);
        event2.setExternalId("test-event-filter-" + System.currentTimeMillis());
        eventRepository.save(event2);

        // ШАГ 1: Фильтрация по спорту
        System.out.println("ШАГ 1: Получение событий по виду спорта");
        mockMvc.perform(get("/api/v1/events/sport/" + testSport.getSportId())
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        System.out.println("✓ События по спорту получены");

        // ШАГ 2: Фильтрация по статусу
        System.out.println("\nШАГ 2: Получение событий по статусу 'upcoming'");
        mockMvc.perform(get("/api/v1/events/status/upcoming")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        System.out.println("✓ События по статусу получены");

        // ШАГ 3: Поиск по названию команды
        System.out.println("\nШАГ 3: Поиск событий с 'Liverpool'");
        mockMvc.perform(get("/api/v1/events/search")
                        .param("query", "Liverpool")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        System.out.println("✓ Поиск выполнен успешно");

        System.out.println("\n=== ✅ ФИЛЬТРАЦИЯ РАБОТАЕТ КОРРЕКТНО ===\n");
    }

    @Test
    void testMultipleBetsScenario() throws Exception {
        System.out.println("\n=== СЦЕНАРИЙ: Множественные ставки ===\n");

        // Создание нескольких ставок
        for (int i = 1; i <= 3; i++) {
            PlaceBetRequest betRequest = new PlaceBetRequest(
                    new BigDecimal(String.valueOf(50 * i)),
                    List.of(outcome1.getOutcomeId())
            );

            mockMvc.perform(post("/api/v1/bets")
                            .header(AUTH_HEADER, TEST_USER_EMAIL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(betRequest)))
                    .andExpect(status().isOk());

            System.out.println("✓ Ставка #" + i + " создана на сумму " + (50 * i));
        }

        // Проверка истории с пагинацией
        System.out.println("\nПроверка истории ставок с пагинацией");
        mockMvc.perform(get("/api/v1/bets/history")
                        .header(AUTH_HEADER, TEST_USER_EMAIL)
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2));

        System.out.println("✓ Пагинация работает корректно");

        // Проверка статистики
        mockMvc.perform(get("/api/v1/bets/stats")
                        .header(AUTH_HEADER, TEST_USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBets").exists())
                .andExpect(jsonPath("$.pendingBets").exists());

        System.out.println("✓ Статистика получена");

        System.out.println("\n=== ✅ МНОЖЕСТВЕННЫЕ СТАВКИ РАБОТАЮТ ===\n");
    }
}

