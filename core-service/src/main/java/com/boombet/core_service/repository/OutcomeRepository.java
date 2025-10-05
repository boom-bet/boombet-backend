package com.boombet.core_service.repository;

import com.boombet.core_service.model.Outcome;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OutcomeRepository extends JpaRepository<Outcome, Long> {
    Optional<Outcome> findByNameAndMarket_MarketId(String name, Long marketId);
}
