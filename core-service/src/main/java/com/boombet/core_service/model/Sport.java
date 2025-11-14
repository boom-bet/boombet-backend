package com.boombet.core_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "sports")
@Data
public class Sport {
    @Id
    @SequenceGenerator(name = "sport_id_seq", sequenceName = "sport_id_seq", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sport_id_seq")
    private Integer sportId;
    private String name;

    // Explicitly adding methods to avoid Lombok issues
    public Integer getSportId() { return sportId; }
    public void setSportId(Integer sportId) { this.sportId = sportId; }
    public void setName(String name) { this.name = name; }
}
