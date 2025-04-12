package com.fraudrisk.service;

import com.fraudrisk.exception.KafkaProducerException;
import com.fraudrisk.model.Transaction;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, Transaction> kafkaTemplate;
    private final MetricsService metricsService;

    @Value("${kafka.topics.transactions}")
    private String transactionTopic;

    @Value("${kafka.producer.timeout-ms:5000}")
    private long producerTimeoutMs;

    @Value("${kafka.producer.sync-send:false}")
    private boolean syncSend;

    /**
     * Send a transaction to Kafka
     * Can be configured for sync or async operation
     */
    public CompletableFuture<Void> sendTransaction(Transaction transaction) {
        // Use transaction ID as key to ensure related transactions go to the same partition
        String key = transaction.getTransactionId().toString();

        Timer.Sample sample = metricsService.startKafkaProducerTimer();

        CompletableFuture<SendResult<String, Transaction>> resultFuture =
                kafkaTemplate.send(transactionTopic, key, transaction);

        // Apply callbacks for metrics
        resultFuture.whenComplete((result, ex) -> {
            metricsService.stopKafkaProducerTimer(sample);

            if (ex == null) {
                metricsService.recordKafkaProducerSuccess();
                log.debug("Transaction sent successfully: id={}, topic={}, partition={}, offset={}",
                        key, result.getRecordMetadata().topic(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                metricsService.recordKafkaProducerFailure();
                log.error("Failed to send transaction with id {}: {}", key, ex.getMessage(), ex);
            }
        });

        // For synchronous operation, wait for completion with timeout
        if (syncSend) {
            try {
                resultFuture.get(producerTimeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new KafkaProducerException("Interrupted while sending transaction", key, e);
            } catch (ExecutionException e) {
                throw new KafkaProducerException("Failed to send transaction", key, e.getCause());
            } catch (TimeoutException e) {
                throw new KafkaProducerException("Timeout while sending transaction", key, e);
            }
        }

        // Convert to CompletableFuture<Void> for easier chaining
        return resultFuture.thenApply(result -> null);
    }
}