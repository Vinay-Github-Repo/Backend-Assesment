package com.example.backend.assessment.dto.response;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AggregateResponse {
    private String tenantId;
    private String bucketStart;
    private String bucketSize;
    private long count;
}
