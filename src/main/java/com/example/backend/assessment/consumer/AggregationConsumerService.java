package com.example.backend.assessment.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class AggregationConsumerService {

    @KafkaListener(topics = "events.validated", groupId = "group")
    public void onAggregate(String message) {
        // placeholder
    }
}
