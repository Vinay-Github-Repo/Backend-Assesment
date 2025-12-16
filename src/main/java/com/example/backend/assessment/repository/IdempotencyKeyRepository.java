package com.example.backend.assessment.repository;

import com.example.backend.assessment.enums.ProcessingStatus;
import com.example.backend.assessment.model.IdempotencyKey;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, String> {

    Optional<IdempotencyKey> findByKey(String key);
    
    Optional<IdempotencyKey> findByTenantIdAndEventId(String tenantId, String eventId);
    
    @Modifying
    @Query("UPDATE IdempotencyKey i SET i.status = :status, i.processedAt = :processedAt " +
           "WHERE i.key = :key")
    void updateStatus(
        @Param("key") String key,
        @Param("status") ProcessingStatus status,
        @Param("processedAt") Instant processedAt
    );
    
    @Modifying
    @Query("DELETE FROM IdempotencyKey i WHERE i.createdAt < :before AND i.status = :status")
    void cleanupOldKeys(
        @Param("before") Instant before,
        @Param("status") ProcessingStatus status
    );
}
