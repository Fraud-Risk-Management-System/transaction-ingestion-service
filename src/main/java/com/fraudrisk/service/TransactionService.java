package com.fraudrisk.service;

import com.fraudrisk.dto.TransactionRequest;
import com.fraudrisk.exception.TransactionProcessingException;
import com.fraudrisk.mapper.TransactionMapper;
import com.fraudrisk.model.Transaction;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class TransactionService {

    @Autowired
    private TransactionMapper transactionMapper;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    @Autowired
    private MetricsService metricsService;

    /**
     * Process a single transaction
     */
    public void processTransaction(TransactionRequest request) {
        try {
            metricsService.recordTransactionReceived();
            Timer.Sample sample = metricsService.startProcessingTimer();

            // Record transaction amount for metrics
            metricsService.recordTransactionAmount(request.getAmount());

            // Convert the DTO to Avro object
            Transaction transaction = transactionMapper.toAvro(request);

            // Send to Kafka
            kafkaProducerService.sendTransaction(transaction)
                    .exceptionally(ex -> {
                        metricsService.recordTransactionFailed();
                        log.error("Error processing transaction {}: {}",
                                request.getTransactionId(), ex.getMessage(), ex);
                        return null;
                    });

            metricsService.stopProcessingTimer(sample);
            metricsService.recordTransactionProcessed();

        } catch (Exception e) {
            metricsService.recordTransactionFailed();
            log.error("Error processing transaction request: {}", e.getMessage(), e);
            throw new TransactionProcessingException("Failed to process transaction: " + e.getMessage(), e);
        }
    }

    /**
     * Process multiple transactions in batch
     */
    public void processTransactions(List<TransactionRequest> requests) {
        List<CompletableFuture<Void>> futures = new ArrayList<>(requests.size());

        for (TransactionRequest request : requests) {
            futures.add(CompletableFuture.runAsync(() -> processTransaction(request)));
        }

        // Wait for all to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .exceptionally(ex -> {
                    log.error("Error processing transaction batch: {}", ex.getMessage(), ex);
                    return null;
                });
    }
}