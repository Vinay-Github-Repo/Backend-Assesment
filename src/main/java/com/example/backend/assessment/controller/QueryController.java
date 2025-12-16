package com.example.backend.assessment.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/query")
public class QueryController {

    @GetMapping
    public ResponseEntity<String> query() {
        return ResponseEntity.ok("ok");
    }
}
