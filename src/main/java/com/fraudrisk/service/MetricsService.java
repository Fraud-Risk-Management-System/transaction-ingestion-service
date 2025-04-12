package com.fraudrisk.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Service for tracking metrics related to the Transaction Ingestion Service
 */
@Service
@Slf4j
public class MetricsService {

    private final MeterRegistry meterRegistry;

    // Counters
    private final Counter transactionsReceivedCounter;
    private final Counter transactionsProcessedCounter;
    private final Counter transactionsFailedCounter;
    private final Counter kafkaProducerSuccessCounter;
    private final Counter kafkaProducerFailureCounter;
    private final Counter batchReceivedCounter;

    // Timers
    private final Timer processingTimer;
    private final Timer kafkaProducerTimer;

    // Distribution summaries
    private final DistributionSummary transactionAmountSummary;
    private final DistributionSummary batchSizeSummary;

    // Service state
    private final AtomicBoolean throttlingEnabled = new AtomicBoolean(false);
    private final AtomicLong lastProcessingTimeMs = new AtomicLong(0);

    // Constructor with proper autowiring
    public MetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters
        this.transactionsReceivedCounter = Counter.builder("transactions.received")
                .description("Number of transactions received")
                .register(meterRegistry);

        this.transactionsProcessedCounter = Counter.builder("transactions.processed")
                .description("Number of transactions successfully processed")
                .register(meterRegistry);

        this.transactionsFailedCounter = Counter.builder("transactions.failed")
                .description("Number of transactions failed to process")
                .register(meterRegistry);

        this.kafkaProducerSuccessCounter = Counter.builder("kafka.producer.success")
                .description("Number of messages successfully sent to Kafka")
                .register(meterRegistry);

        this.kafkaProducerFailureCounter = Counter.builder("kafka.producer.failure")
                .description("Number of messages failed to send to Kafka")
                .register(meterRegistry);

        this.batchReceivedCounter = Counter.builder("transactions.batch.received")
                .description("Number of batch requests received")
                .register(meterRegistry);

        // Initialize timers
        this.processingTimer = Timer.builder("transactions.processing.time")
                .description("Time taken to process transactions")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        this.kafkaProducerTimer = Timer.builder("kafka.producer.time")
                .description("Time taken to send messages to Kafka")
                .publishPercentiles(0.5, 0.95, 0.99)
                .register(meterRegistry);

        // Initialize distribution summaries
        this.transactionAmountSummary = DistributionSummary.builder("transactions.amount")
                .description("Distribution of transaction amounts")
                .baseUnit("dollars")
                .publishPercentiles(0.5, 0.75, 0.9, 0.95, 0.99)
                .register(meterRegistry);

        this.batchSizeSummary = DistributionSummary.builder("transactions.batch.size")
                .description("Distribution of batch sizes")
                .register(meterRegistry);
    }

    /**
     * Record a transaction received
     */
    public void recordTransactionReceived() {
        transactionsReceivedCounter.increment();
    }

    /**
     * Record a transaction processed successfully
     */
    public void recordTransactionProcessed() {
        transactionsProcessedCounter.increment();
    }

    /**
     * Record a transaction failed to process
     */
    public void recordTransactionFailed() {
        transactionsFailedCounter.increment();
    }

    /**
     * Record a batch received
     */
    public void recordBatchReceived(int batchSize) {
        batchReceivedCounter.increment();
        batchSizeSummary.record(batchSize);
    }

    /**
     * Record a successful Kafka producer send
     */
    public void recordKafkaProducerSuccess() {
        kafkaProducerSuccessCounter.increment();
    }

    /**
     * Record a failed Kafka producer send
     */
    public void recordKafkaProducerFailure() {
        kafkaProducerFailureCounter.increment();
    }

    /**
     * Record transaction processing time
     */
    public Timer.Sample startProcessingTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop transaction processing timer
     */
    public void stopProcessingTimer(Timer.Sample sample) {
        long timeNanos = sample.stop(processingTimer);
        lastProcessingTimeMs.set(TimeUnit.NANOSECONDS.toMillis(timeNanos));
    }

    /**
     * Record Kafka producer send time
     */
    public Timer.Sample startKafkaProducerTimer() {
        return Timer.start(meterRegistry);
    }

    /**
     * Stop Kafka producer send timer
     */
    public void stopKafkaProducerTimer(Timer.Sample sample) {
        sample.stop(kafkaProducerTimer);
    }

    /**
     * Record transaction amount
     */
    public void recordTransactionAmount(BigDecimal amount) {
        if (amount != null) {
            transactionAmountSummary.record(amount.doubleValue());
        }
    }

    /**
     * Get the count of transactions received
     */
    public long getTransactionsReceivedCount() {
        return (long) transactionsReceivedCounter.count();
    }

    /**
     * Get the count of transactions processed
     */
    public long getTransactionsProcessedCount() {
        return (long) transactionsProcessedCounter.count();
    }

    /**
     * Get the count of transactions failed
     */
    public long getTransactionsFailedCount() {
        return (long) transactionsFailedCounter.count();
    }

    /**
     * Get the average processing time in milliseconds
     */
    public double getAverageProcessingTimeMs() {
        return processingTimer.mean(TimeUnit.MILLISECONDS);
    }

    /**
     * Get the last processing time in milliseconds
     */
    public long getLastProcessingTimeMs() {
        return lastProcessingTimeMs.get();
    }

    /**
     * Set throttling state
     */
    public void setThrottlingEnabled(boolean enabled) {
        throttlingEnabled.set(enabled);
        log.info("Transaction throttling set to: {}", enabled);
    }

    /**
     * Check if throttling is enabled
     */
    public boolean isThrottlingEnabled() {
        return throttlingEnabled.get();
    }
}