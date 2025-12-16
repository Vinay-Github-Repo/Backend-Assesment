package com.example.backend.assessment.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

import com.example.backend.assessment.enums.BucketSize;

@Entity
@Table(name = "aggregates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Aggregate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tenant_id", length = 50, nullable = false)
    private String tenantId;
    
    @Column(name = "bucket_start", nullable = false)
    private Instant bucketStart;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "bucket_size", length = 10, nullable = false)
    private BucketSize bucketSize;
    
    @Column(name = "source", length = 50)
    private String source;
    
    @Column(name = "event_type", length = 50)
    private String eventType;
    
    @Column(name = "count", nullable = false)
    private Long count = 0L;
    
    @Column(name = "first_seen")
    private Instant firstSeen;
    
    @Column(name = "last_seen")
    private Instant lastSeen;
    
    @Column(name = "last_aggregated_at")
    private Instant lastAggregatedAt;
    
    @Version
    private Long version;
}
