package com.example.backend.assessment.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private String eventId;
    private String tenantId;
}
