package com.boombet.core_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "events")
@Data
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @ManyToOne
    @JoinColumn(name = "sport_id", nullable = false)
    private Sport sport;

    @Column(name = "team_a")
    private String teamA;

    @Column(name = "team_b")
    private String teamB;

    @Column(name = "start_time")
    private OffsetDateTime startTime;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "result")
    private String result;

    @Column(name = "external_id", unique = true)
    private String externalId;
}
