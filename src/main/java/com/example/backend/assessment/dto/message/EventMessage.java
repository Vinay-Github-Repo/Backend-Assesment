package com.example.backend.assessment.dto.message;

import lombok.*;

import java.time.Instant;
import java.util.Map;

@Data
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventMessage {
    private String eventId;
    private String tenantId;
    private String source;
    private String eventType;
    private Instant timestamp;
    private Map<String, Object> payload;
    private String correlationId;
    private Instant ingestedAt;
    private Map<String, String> headers;
}
