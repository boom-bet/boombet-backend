package com.boombet.core_service.repository;

import com.boombet.core_service.model.Bet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {
    @Query("SELECT DISTINCT b FROM Bet b JOIN BetSelection bs ON b.betId = bs.betId JOIN Outcome o ON bs.outcomeId = o.outcomeId WHERE o.market.event.eventId = :eventId AND b.status = 'pending'")
    List<Bet> findPendingBetsByEventId(Long eventId);
}
