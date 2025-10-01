package com.boombet.core_service.repository;

import com.boombet.core_service.model.BetSelection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BetSelectionRepository extends JpaRepository<BetSelection, Long> {
}
