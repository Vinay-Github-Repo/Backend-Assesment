package com.example.backend.assessment.service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.example.backend.assessment.dto.message.EventMessage;
import com.example.backend.assessment.dto.request.EventRequest;
import com.example.backend.assessment.enums.ProcessingStatus;
import com.example.backend.assessment.model.IdempotencyKey;

import java.util.List;
import java.util.Optional;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducerService {
    private final KafkaTemplate<String, EventMessage> kafkaTemplate;
    private final IdempotencyService idempotencyService;
    
    @Value("${app.kafka.topics.raw-events}")
    private String rawEventsTopic;
    
    @Transactional
    public CompletableFuture<IngestionResult> produceEvent(EventRequest request) {
        String idempotencyKey = generateIdempotencyKey(request);
        
        // Check idempotency
        Optional<IdempotencyKey> existing = idempotencyService.findByKey(idempotencyKey);
        if (existing.isPresent() && existing.get().getStatus() == ProcessingStatus.COMPLETED) {
            log.info("Duplicate event detected: {}", request.getEventId());
            return CompletableFuture.completedFuture(
                IngestionResult.duplicate(request.getEventId()));
        }
        
        // Create idempotency record
        if (existing.isEmpty()) {
            idempotencyService.createKey(idempotencyKey, request.getTenantId(), 
                request.getEventId(), ProcessingStatus.PROCESSING);
        }
        
        // Build event message
        EventMessage message = EventMessage.builder()
                .eventId(request.getEventId())
                .tenantId(request.getTenantId())
                .source(request.getSource())
                .eventType(request.getEventType())
                .timestamp(request.getTimestamp())
                .payload(request.getPayload())
                .correlationId(MDC.get("correlationId"))
                .ingestedAt(Instant.now())
                .build();
        
        // Create producer record with tenant as key
        ProducerRecord<String, EventMessage> record = new ProducerRecord<>(
                rawEventsTopic,
                request.getTenantId(),
                message
        );
        
        // Add headers
        record.headers().add("eventId", request.getEventId().getBytes(StandardCharsets.UTF_8));
        record.headers().add("tenantId", request.getTenantId().getBytes(StandardCharsets.UTF_8));
        
        // Send to Kafka
        return kafkaTemplate.send(record)
                .thenApply(result -> {
                    log.info("Event produced: eventId={}, partition={}, offset={}", 
                        request.getEventId(), 
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                    
                    return IngestionResult.accepted(request.getEventId());
                })
                .exceptionally(ex -> {
                    log.error("Failed to produce event: {}", request.getEventId(), ex);
                    idempotencyService.updateStatus(idempotencyKey, ProcessingStatus.FAILED);
                    throw new RuntimeException("Failed to produce event", ex);
                });
    }
    
    @Transactional
    public CompletableFuture<BulkIngestionResult> produceBulkEvents(List<EventRequest> requests) {
        if (requests.size() > 5000) {
            throw new IllegalArgumentException("Maximum 5000 events allowed");
        }
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();
        int successCount = 0;
        int failedCount = 0;
        
        for (EventRequest request : requests) {
            CompletableFuture<Void> future = produceEvent(request)
                    .thenAccept(result -> log.debug("Bulk event processed: {}", result.getEventId()))
                    .exceptionally(ex -> {
                        log.error("Failed to produce bulk event", ex);
                        return null;
                    });
            futures.add(future);
        }
        
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> {
                    long success = futures.stream()
                            .filter(f -> !f.isCompletedExceptionally())
                            .count();
                    return BulkIngestionResult.builder()
                            .totalReceived(requests.size())
                            .successCount((int) success)
                            .failedCount(requests.size() - (int) success)
                            .build();
                });
    }
    
    private String generateIdempotencyKey(EventRequest request) {
        return String.format("%s:%s", request.getTenantId(), request.getEventId());
    }
    
    // Inner classes for results
    @lombok.Data
    @lombok.Builder
    public static class IngestionResult {
        private String eventId;
        private String status;
        private boolean accepted;
        private boolean duplicate;
        
        public static IngestionResult accepted(String eventId) {
            return IngestionResult.builder()
                    .eventId(eventId)
                    .status("ACCEPTED")
                    .accepted(true)
                    .duplicate(false)
                    .build();
        }
        
        public static IngestionResult duplicate(String eventId) {
            return IngestionResult.builder()
                    .eventId(eventId)
                    .status("DUPLICATE")
                    .accepted(false)
                    .duplicate(true)
                    .build();
        }
    }
    
    @lombok.Data
    @lombok.Builder
    public static class BulkIngestionResult {
        private int totalReceived;
        private int successCount;
        private int failedCount;
    }
}
