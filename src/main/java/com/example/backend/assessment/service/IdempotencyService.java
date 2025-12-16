package com.example.backend.assessment.service;

import java.time.Instant;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.backend.assessment.enums.ProcessingStatus;
import com.example.backend.assessment.model.IdempotencyKey;
import com.example.backend.assessment.repository.IdempotencyKeyRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class IdempotencyService {
    private final IdempotencyKeyRepository repository;
    
    @Transactional
    public void createKey(String key, String tenantId, String eventId, ProcessingStatus status) {
        IdempotencyKey idempotencyKey = IdempotencyKey.builder()
                .key(key)
                .tenantId(tenantId)
                .eventId(eventId)
                .status(status)
                .createdAt(Instant.now())
                .build();
        
        repository.save(idempotencyKey);
        log.debug("Created idempotency key: {}", key);
    }
    
    @Transactional(readOnly = true)
    public Optional<IdempotencyKey> findByKey(String key) {
        return repository.findByKey(key);
    }
    
    @Transactional(readOnly = true)
    public boolean isAlreadyProcessed(String key) {
        return repository.findByKey(key)
                .map(k -> k.getStatus() == ProcessingStatus.COMPLETED)
                .orElse(false);
    }
    
    @Transactional
    public void updateStatus(String key, ProcessingStatus status) {
        repository.updateStatus(key, status, Instant.now());
        log.debug("Updated idempotency key status: {} -> {}", key, status);
    }
}
