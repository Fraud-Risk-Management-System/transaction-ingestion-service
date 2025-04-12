// src/main/java/com/fraudrisk/dto/BatchResponse.java
package com.fraudrisk.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Response for batch transaction processing
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchResponse {
    private int batchSize;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Instant timestamp;

    private String message;
}