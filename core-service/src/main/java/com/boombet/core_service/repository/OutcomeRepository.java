package com.boombet.core_service.repository;

import com.boombet.core_service.model.Outcome;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OutcomeRepository extends JpaRepository<Outcome, Long> {
    Optional<Outcome> findByMarket_MarketIdAndName(Long marketId, String name);
}
