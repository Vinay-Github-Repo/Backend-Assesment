package com.example.backend.assessment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metrics")
public class MetricsController {

    @GetMapping
    public ResponseEntity<String> metrics() {
        return ResponseEntity.ok("metrics");
    }
}
