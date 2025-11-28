package com.boombet.core_service.repository;

import com.boombet.core_service.model.Bet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface BetRepository extends JpaRepository<Bet, Long> {
    @Query("SELECT DISTINCT b FROM Bet b JOIN BetSelection bs ON b.betId = bs.betId JOIN Outcome o ON bs.outcomeId = o.outcomeId WHERE o.market.event.eventId = :eventId AND b.status = 'pending'")
    List<Bet> findPendingBetsByEventId(Long eventId);

    // Все ставки пользователя с пагинацией
    Page<Bet> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    // Ставки пользователя по статусу
    Page<Bet> findByUserIdAndStatusOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);

    // Ставки пользователя за период
    @Query("SELECT b FROM Bet b WHERE b.userId = :userId AND b.createdAt BETWEEN :startDate AND :endDate ORDER BY b.createdAt DESC")
    Page<Bet> findByUserIdAndDateRange(
        @Param("userId") Long userId,
        @Param("startDate") OffsetDateTime startDate,
        @Param("endDate") OffsetDateTime endDate,
        Pageable pageable
    );

    // Подсчет ставок пользователя
    long countByUserId(Long userId);

    // Подсчет ставок по статусу
    long countByUserIdAndStatus(Long userId, String status);
}
