package com.boombet.core_service.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.boombet.core_service.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByStatus(String status);
    
    List<Event> findByStatus(String status);
    
    Optional<Event> findByExternalId(String externalId);

    // Фильтрация по спорту
    Page<Event> findBySportSportIdOrderByStartTimeAsc(Integer sportId, Pageable pageable);

    // Фильтрация по статусу с пагинацией
    Page<Event> findByStatusOrderByStartTimeAsc(String status, Pageable pageable);

    // Фильтрация по спорту и статусу
    Page<Event> findBySportSportIdAndStatusOrderByStartTimeAsc(Integer sportId, String status, Pageable pageable);

    // События в диапазоне дат
    @Query("SELECT e FROM Event e WHERE e.startTime BETWEEN :startDate AND :endDate ORDER BY e.startTime ASC")
    Page<Event> findByDateRange(
        @Param("startDate") OffsetDateTime startDate,
        @Param("endDate") OffsetDateTime endDate,
        Pageable pageable
    );

    // Поиск по названиям команд
    @Query("SELECT e FROM Event e WHERE LOWER(e.teamA) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.teamB) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY e.startTime ASC")
    Page<Event> searchByTeams(@Param("query") String query, Pageable pageable);

    // Предстоящие события (status = 'upcoming' и startTime > now)
    @Query("SELECT e FROM Event e WHERE e.status = 'upcoming' AND e.startTime > :currentTime ORDER BY e.startTime ASC")
    List<Event> findUpcomingEvents(@Param("currentTime") OffsetDateTime currentTime);

    // Комплексная фильтрация
    @Query("SELECT e FROM Event e WHERE " +
           "(:sportId IS NULL OR e.sport.sportId = :sportId) AND " +
           "(:status IS NULL OR e.status = :status) AND " +
           "(:startDate IS NULL OR e.startTime >= :startDate) AND " +
           "(:endDate IS NULL OR e.startTime <= :endDate) AND " +
           "(:query IS NULL OR LOWER(e.teamA) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(e.teamB) LIKE LOWER(CONCAT('%', :query, '%'))) " +
           "ORDER BY e.startTime ASC")
    Page<Event> findByFilters(
        @Param("sportId") Integer sportId,
        @Param("status") String status,
        @Param("startDate") OffsetDateTime startDate,
        @Param("endDate") OffsetDateTime endDate,
        @Param("query") String query,
        Pageable pageable
    );
}
