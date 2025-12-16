package com.example.backend.assessment.consumer;

import java.time.Instant;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.MDC;
import com.example.backend.assessment.model.Event;

import com.example.backend.assessment.dto.message.EventMessage;
import com.example.backend.assessment.enums.ProcessingStatus;
import com.example.backend.assessment.repository.EventRepository;
import com.example.backend.assessment.service.IdempotencyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventConsumerService {

    private final EventRepository eventRepository;
    private final IdempotencyService idempotencyService;
    private final KafkaTemplate<String, EventMessage> kafkaTemplate;
    
    @Value("${app.kafka.topics.validated-events}")
    private String validatedEventsTopic;
    
    @Value("${app.kafka.topics.dlq}")
    private String dlqTopic;
    
    @KafkaListener(
        topics = "${app.kafka.topics.raw-events}",
        groupId = "${spring.kafka.consumer.group-id}",
        containerFactory = "kafkaListenerContainerFactory"
    )
    @Transactional
    public void consumeRawEvents(
            @Payload EventMessage message,
            @Header(KafkaHeaders.RECEIVED_TOPIC) String topic,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {
        
        MDC.put("eventId", message.getEventId());
        MDC.put("tenantId", message.getTenantId());
        
        try {
            log.info("Processing event: eventId={}, partition={}, offset={}", 
                message.getEventId(), partition, offset);
            
            // Validate event
            validateEvent(message);
            
            // Check idempotency
            String idempotencyKey = generateIdempotencyKey(message);
            if (idempotencyService.isAlreadyProcessed(idempotencyKey)) {
                log.info("Event already processed, skipping: {}", message.getEventId());
                acknowledgment.acknowledge();
                return;
            }
            
            // Map and persist event
            Event event = mapToEntity(message);
            event.setKafkaTopic(topic);
            event.setKafkaPartition(partition);
            event.setKafkaOffset(offset);
            
            eventRepository.save(event);
            
            // Update idempotency status
            idempotencyService.updateStatus(idempotencyKey, ProcessingStatus.COMPLETED);
            
            // Produce to validated topic
            kafkaTemplate.send(validatedEventsTopic, message.getTenantId(), message);
            
            // Acknowledge
            acknowledgment.acknowledge();
            
            log.info("Event processed successfully: {}", message.getEventId());
            
        } catch (ValidationException e) {
            log.error("Validation failed: {}", message.getEventId(), e);
            sendToDLQ(message, partition, offset, "VALIDATION_FAILED", e.getMessage());
            acknowledgment.acknowledge();
            
        } catch (DataIntegrityViolationException e) {
            log.warn("Duplicate event in database: {}", message.getEventId());
            acknowledgment.acknowledge();
            
        } catch (Exception e) {
            log.error("Error processing event: {}", message.getEventId(), e);
            sendToDLQ(message, partition, offset, "PROCESSING_ERROR", e.getMessage());
            acknowledgment.acknowledge();
            
        } finally {
            MDC.clear();
        }
    }
    
    private void validateEvent(EventMessage message) {
        if (message.getTimestamp().isAfter(Instant.now().plusSeconds(60))) {
            throw new ValidationException("timestamp cannot be in the future");
        }
        // Add more validation as needed
    }
    
    private void sendToDLQ(EventMessage message, int partition, long offset, 
                          String errorType, String errorMessage) {
        try {
            if (message.getHeaders() == null) {
                message.setHeaders(new java.util.HashMap<>());
            }
            message.getHeaders().put("errorType", errorType);
            message.getHeaders().put("errorMessage", errorMessage);
            message.getHeaders().put("originalPartition", String.valueOf(partition));
            message.getHeaders().put("originalOffset", String.valueOf(offset));
            message.getHeaders().put("failedAt", Instant.now().toString());
            
            kafkaTemplate.send(dlqTopic, message.getTenantId(), message);
            log.info("Sent to DLQ: eventId={}, errorType={}", message.getEventId(), errorType);
        } catch (Exception e) {
            log.error("Failed to send to DLQ: {}", message.getEventId(), e);
        }
    }
    
    private Event mapToEntity(EventMessage message) {
        return Event.builder()
                .eventId(message.getEventId())
                .tenantId(message.getTenantId())
                .source(message.getSource())
                .eventType(message.getEventType())
                .timestamp(message.getTimestamp())
                .payload(message.getPayload())
                .build();
    }
    
    private String generateIdempotencyKey(EventMessage message) {
        return String.format("%s:%s", message.getTenantId(), message.getEventId());
    }
    
    // Validation exception
    public static class ValidationException extends RuntimeException {
        public ValidationException(String message) {
            super(message);
        }
    }
}
