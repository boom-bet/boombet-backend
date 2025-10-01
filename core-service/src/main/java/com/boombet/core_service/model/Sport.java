package com.boombet.core_service.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "sports")
@Data
public class Sport {
    @Id
    private Integer sportId;
    private String name;
}
