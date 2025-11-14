package com.boombet.core_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.boombet.core_service.model.Sport;

@Repository
public interface SportRepository extends JpaRepository<Sport, Integer> {
    Optional<Sport> findByName(String name);
}
