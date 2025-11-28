package com.boombet.core_service.controller;

import com.boombet.core_service.model.Sport;
import com.boombet.core_service.repository.SportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/sports")
public class SportController {

    @Autowired
    private SportRepository sportRepository;

    /**
     * Получить список всех видов спорта
     */
    @GetMapping
    public ResponseEntity<List<Sport>> getAllSports() {
        List<Sport> sports = sportRepository.findAll();
        return ResponseEntity.ok(sports);
    }
}

