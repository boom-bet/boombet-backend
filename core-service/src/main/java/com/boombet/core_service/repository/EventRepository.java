package com.boombet.core_service.repository;

import com.boombet.core_service.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findAllByStatus(String status);
    
    Optional<Event> findByExternalId(String externalId);
}
