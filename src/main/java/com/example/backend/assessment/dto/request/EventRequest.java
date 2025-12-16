package com.example.backend.assessment.dto.request;

import lombok.*;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {
    @NotBlank(message = "eventId is required")
    @Size(max = 100, message = "eventId must not exceed 100 characters")
    private String eventId;
    
    @NotBlank(message = "tenantId is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Invalid tenantId format")
    @Size(max = 50, message = "tenantId must not exceed 50 characters")
    private String tenantId;
    
    @NotBlank(message = "source is required")
    @Size(max = 50, message = "source must not exceed 50 characters")
    private String source;
    
    @NotBlank(message = "eventType is required")
    @Size(max = 50, message = "eventType must not exceed 50 characters")
    private String eventType;
    
    @NotNull(message = "timestamp is required")
    @PastOrPresent(message = "timestamp cannot be in the future")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private Instant timestamp;
    
    @NotNull(message = "payload is required")
    @Size(max = 50, message = "payload cannot have more than 50 keys")
    private Map<String, Object> payload;
}
