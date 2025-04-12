package com.fraudrisk.controller;

import com.fraudrisk.dto.BatchResponse;
import com.fraudrisk.dto.TransactionRequest;
import com.fraudrisk.service.MetricsService;
import com.fraudrisk.service.TransactionService;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
@Validated
public class TransactionController {

    private final TransactionService transactionService;
    private final MetricsService metricsService;

    /**
     * Ingest a single transaction
     */
    @PostMapping
    @Timed(value = "api.transaction.single", description = "Time taken to process a single transaction API call")
    public ResponseEntity<?> ingestTransaction(@Valid @RequestBody TransactionRequest request) {
        log.debug("Received transaction request: {}", request.getTransactionId());

        Timer.Sample sample = metricsService.startProcessingTimer();

        // Record metrics for this transaction
        metricsService.recordTransactionReceived();
        metricsService.recordTransactionAmount(request.getAmount());

        try {
            transactionService.processTransaction(request);
            metricsService.stopProcessingTimer(sample);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body("Transaction Accepted");
        } catch (Exception e) {
            metricsService.recordTransactionFailed();
            metricsService.stopProcessingTimer(sample);
            throw e;
        }
    }

    /**
     * Ingest a batch of transactions
     * Limits batch size and processes asynchronously
     */
    @PostMapping("/batch")
    @Timed(value = "api.transaction.batch", description = "Time taken to process a batch of transactions API call")
    public ResponseEntity<BatchResponse> ingestBatchTransactions(
            @Valid @NotEmpty(message = "Batch cannot be empty")
            @Size(max = 1000, message = "Batch size cannot exceed 1000 transactions")
            @RequestBody List<@Valid TransactionRequest> requests) {

        log.debug("Received batch with {} transactions", requests.size());

        // Record batch metrics
        metricsService.recordBatchReceived(requests.size());

        // Process asynchronously
        CompletableFuture.runAsync(() -> transactionService.processTransactions(requests));

        // Return immediately with accepted status
        BatchResponse response = BatchResponse.builder()
                .batchSize(requests.size())
                .timestamp(Instant.now())
                .message("Batch accepted for processing")
                .build();

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Service is healthy");
    }

    /**
     * Metrics endpoint for basic stats
     */
    @GetMapping("/metrics/basic")
    public ResponseEntity<Map<String, Object>> basicMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Get actual metrics from the MetricsService
        metrics.put("transactionsReceived", metricsService.getTransactionsReceivedCount());
        metrics.put("transactionsProcessed", metricsService.getTransactionsProcessedCount());
        metrics.put("transactionsFailed", metricsService.getTransactionsFailedCount());
        metrics.put("averageProcessingTimeMs", metricsService.getAverageProcessingTimeMs());
        metrics.put("apiHealthy", true);

        return ResponseEntity.ok(metrics);
    }

    /**
     * Throttle transactions if system is overloaded
     */
    @PutMapping("/throttle")
    @ResponseStatus(HttpStatus.OK)
    public void setThrottleStatus(@RequestParam boolean enabled) {
        // Implementation would typically control a throttling mechanism
        log.info("Transaction throttling set to: {}", enabled);
        metricsService.setThrottlingEnabled(enabled);
    }
}