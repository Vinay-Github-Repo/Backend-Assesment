package com.example.backend.assessment.repository;

import com.example.backend.assessment.enums.BucketSize;
import com.example.backend.assessment.model.Aggregate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AggregateRepository extends JpaRepository<Aggregate, Long> {
    @Query("SELECT a FROM Aggregate a WHERE " +
           "a.tenantId = :tenantId AND " +
           "a.bucketStart = :bucketStart AND " +
           "a.bucketSize = :bucketSize AND " +
           "a.source = :source AND " +
           "a.eventType = :eventType")
    Optional<Aggregate> findByUniqueKey(
        @Param("tenantId") String tenantId,
        @Param("bucketStart") Instant bucketStart,
        @Param("bucketSize") BucketSize bucketSize,
        @Param("source") String source,
        @Param("eventType") String eventType
    );
    
    @Query("SELECT a FROM Aggregate a WHERE " +
           "a.tenantId = :tenantId AND " +
           "a.bucketSize = :bucketSize AND " +
           "a.bucketStart BETWEEN :from AND :to " +
           "ORDER BY a.bucketStart DESC")
    List<Aggregate> findMetrics(
        @Param("tenantId") String tenantId,
        @Param("bucketSize") BucketSize bucketSize,
        @Param("from") Instant from,
        @Param("to") Instant to
    );
}
