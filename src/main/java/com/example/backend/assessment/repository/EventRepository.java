package com.example.backend.assessment.repository;

import com.example.backend.assessment.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, String> {
    @Query("SELECT e FROM Event e WHERE e.tenantId = :tenantId " +
           "AND e.timestamp BETWEEN :from AND :to " +
           "ORDER BY e.timestamp DESC")
    Page<Event> findByTenantAndTimeRange(
        @Param("tenantId") String tenantId,
        @Param("from") Instant from,
        @Param("to") Instant to,
        Pageable pageable
    );
    
    List<Event> findByTenantIdAndSourceAndEventType(
        String tenantId, 
        String source, 
        String eventType
    );
    
    long countByTenantId(String tenantId);
}
