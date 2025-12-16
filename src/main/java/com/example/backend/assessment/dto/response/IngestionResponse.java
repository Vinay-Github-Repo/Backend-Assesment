package com.example.backend.assessment.dto.response;

import lombok.*;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IngestionResponse {
    private String eventId;
    private String status; // ACCEPTED, DUPLICATE
    private String message;
    private Instant timestamp;
}
