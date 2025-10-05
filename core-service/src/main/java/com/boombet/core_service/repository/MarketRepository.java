package com.boombet.core_service.repository;

import com.boombet.core_service.model.Market;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {
    List<Market> findAllByEvent_EventId(Long eventId);

    Optional<Market> findByNameAndEvent_EventId(String name, Long eventId);
}
