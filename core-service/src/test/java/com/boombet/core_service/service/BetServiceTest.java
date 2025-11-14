package com.boombet.core_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import com.boombet.core_service.config.KafkaTopicConfig;
import com.boombet.core_service.dto.PlaceBetRequest;
import com.boombet.core_service.model.Bet;
import com.boombet.core_service.model.Event;
import com.boombet.core_service.model.Market;
import com.boombet.core_service.model.Outcome;
import com.boombet.core_service.model.Transaction;
import com.boombet.core_service.model.User;
import com.boombet.core_service.repository.BetRepository;
import com.boombet.core_service.repository.BetSelectionRepository;
import com.boombet.core_service.repository.OutcomeRepository;
import com.boombet.core_service.repository.TransactionRepository;
import com.boombet.core_service.repository.UserRepository;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class BetServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OutcomeRepository outcomeRepository;
    @Mock
    private BetRepository betRepository;
    @Mock
    private BetSelectionRepository betSelectionRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private SettlementStrategy settlementStrategy;

    private BetService betService;

    @BeforeEach
    void setUp() {
        when(settlementStrategy.getMarketName()).thenReturn("Match Winner");
        betService = new BetService(
                kafkaTemplate,
                userRepository,
                outcomeRepository,
                betRepository,
                betSelectionRepository,
                transactionRepository,
                List.of(settlementStrategy)
        );
    }

    @Test
    void placeBet_debitsBalanceAndPersistsEntities() {
        PlaceBetRequest request = new PlaceBetRequest(new BigDecimal("10.00"), List.of(1L));
        User user = new User();
        user.setBalance(new BigDecimal("50.00"));
        user.setEmail("bettor@boom.bet");
        user.setUserId(5L);

        Outcome outcome = new Outcome();
        outcome.setOutcomeId(1L);
        outcome.setCurrentOdds(new BigDecimal("2.00"));
        Market market = new Market();
        market.setName("Match Winner");
        Event event = new Event();
        event.setStatus("upcoming");
        market.setEvent(event);
        outcome.setMarket(market);

        when(userRepository.findByEmail("bettor@boom.bet")).thenReturn(Optional.of(user));
        when(outcomeRepository.findAllById(request.outcomeIds())).thenReturn(List.of(outcome));
        when(betRepository.save(any(Bet.class))).thenAnswer(invocation -> {
            Bet bet = invocation.getArgument(0);
            bet.setBetId(99L);
            bet.setCreatedAt(OffsetDateTime.now());
            return bet;
        });
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Bet placedBet = betService.placeBet(request, "bettor@boom.bet");

        assertThat(placedBet.getBetId()).isEqualTo(99L);
        assertThat(placedBet.getPotentialPayout()).isEqualByComparingTo("20.00");
        assertThat(user.getBalance()).isEqualByComparingTo("40.00");

        verify(betSelectionRepository).save(argThat(selection ->
                selection.getBetId().equals(99L)
                        && selection.getOutcomeId().equals(1L)
                        && selection.getOddsAtPlacement().compareTo(new BigDecimal("2.00")) == 0
        ));

        verify(transactionRepository).save(any(Transaction.class));
        verify(kafkaTemplate).send(eq(KafkaTopicConfig.TOPIC_NOTIFICATIONS), contains("placed a new bet"));
    }

    @Test
    void placeBet_failsWhenBalanceInsufficient() {
        PlaceBetRequest request = new PlaceBetRequest(new BigDecimal("100.00"), List.of(1L));
        User user = new User();
        user.setBalance(new BigDecimal("10.00"));
        user.setEmail("bettor@boom.bet");
        user.setUserId(5L);

        when(userRepository.findByEmail("bettor@boom.bet")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> betService.placeBet(request, "bettor@boom.bet"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Insufficient balance");

        verifyNoInteractions(outcomeRepository, betRepository, betSelectionRepository, transactionRepository, kafkaTemplate);
    }
}
