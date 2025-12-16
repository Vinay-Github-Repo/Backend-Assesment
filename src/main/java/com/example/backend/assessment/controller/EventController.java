package com.example.backend.assessment.controller;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.UUID;

import com.example.backend.assessment.dto.request.EventRequest;
import com.example.backend.assessment.dto.response.IngestionResponse;
import com.example.backend.assessment.service.EventProducerService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EventController {

    private final EventProducerService producerService;
    
    @PostMapping
    public ResponseEntity<IngestionResponse> ingestEvent(
            @Valid @RequestBody EventRequest request) {
        
        log.info("Received event: eventId={}, tenantId={}", 
            request.getEventId(), request.getTenantId());
        
        EventProducerService.IngestionResult result = 
            producerService.produceEvent(request).join();
        
        HttpStatus status = result.isAccepted() ? HttpStatus.ACCEPTED : HttpStatus.OK;
        
        return ResponseEntity.status(status)
                .body(IngestionResponse.builder()
                        .eventId(result.getEventId())
                        .status(result.getStatus())
                        .message(result.isDuplicate() ? "Duplicate event" : "Event accepted")
                        .timestamp(Instant.now())
                        .build());
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<BulkIngestionResponse> ingestBulkEvents(
            @Valid @RequestBody @Size(max = 5000) List<EventRequest> requests) {
        
        log.info("Received bulk ingestion: count={}", requests.size());
        
        String jobId = UUID.randomUUID().toString();
        
        producerService.produceBulkEvents(requests)
                .thenAccept(result -> {
                    log.info("Bulk ingestion completed: jobId={}, success={}, failed={}", 
                        jobId, result.getSuccessCount(), result.getFailedCount());
                });
        
        return ResponseEntity.accepted()
                .body(BulkIngestionResponse.builder()
                        .jobId(jobId)
                        .status("PROCESSING")
                        .totalReceived(requests.size())
                        .message("Bulk events accepted for processing")
                        .build());
    }
    
    @lombok.Data
    @lombok.Builder
    public static class BulkIngestionResponse {
        private String jobId;
        private String status;
        private int totalReceived;
        private String message;
    }
}
