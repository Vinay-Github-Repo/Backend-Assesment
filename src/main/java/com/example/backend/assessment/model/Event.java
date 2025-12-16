package com.example.backend.assessment.model;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonSubTypes.Type;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {
    @Id
    @Column(name = "event_id", length = 100, nullable = false)
    private String eventId;
    
    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;
    
    @Column(name = "source", length = 50, nullable = false)
    private String source;
    
    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType;
    
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    
    @Type(JsonType.class)
    @Column(name = "payload", columnDefinition = "jsonb")
    private Map<String, Object> payload;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @Column(name = "kafka_topic", length = 100)
    private String kafkaTopic;
    
    @Column(name = "kafka_partition")
    private Integer kafkaPartition;
    
    @Column(name = "kafka_offset")
    private Long kafkaOffset;
    
    @Column(name = "processing_attempts", nullable = false)
    private Integer processingAttempts = 0;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (processingAttempts == null) {
            processingAttempts = 0;
        }
    }
}
