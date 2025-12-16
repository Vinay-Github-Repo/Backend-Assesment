package com.example.backend.assessment.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.*;

import java.time.Instant;

import com.example.backend.assessment.enums.ProcessingStatus;

@Entity
@Table(name = "idempotency_keys")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdempotencyKey {
    @Id
    @Column(name = "idempotency_key", length = 200, nullable = false)
    private String key;
    
    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;
    
    @Column(name = "event_id", length = 100, nullable = false)
    private String eventId;
    
    @Column(name = "status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ProcessingStatus status;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "processed_at")
    private Instant processedAt;
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }
}
