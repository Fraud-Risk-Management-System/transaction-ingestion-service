package com.fraudrisk.service;

import com.fraudrisk.dto.TransactionRequest;
import com.fraudrisk.exception.TransactionProcessingException;
import com.fraudrisk.mapper.TransactionMapper;
import com.fraudrisk.model.Transaction;
import io.micrometer.core.instrument.Timer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionMapper transactionMapper;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @Mock
    private MetricsService metricsService;

    @Mock
    private Timer.Sample timerSample;

    @InjectMocks
    private TransactionService transactionService;

    private TransactionRequest validRequest;
    private Transaction mockTransaction;

    @BeforeEach
    void setUp() {
        validRequest = createValidTransactionRequest();
        mockTransaction = mock(Transaction.class);

        when(metricsService.startProcessingTimer()).thenReturn(timerSample);
    }

    @Test
    void processTransaction_ValidRequest_SuccessfullyProcesses() {
        // Arrange
        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(transactionMapper.toAvro(any())).thenReturn(mockTransaction);
        when(kafkaProducerService.sendTransaction(any())).thenReturn(future);

        // Act & Assert
        assertDoesNotThrow(() -> transactionService.processTransaction(validRequest));

        // Verify
        verify(metricsService).recordTransactionReceived();
        verify(metricsService).recordTransactionAmount(validRequest.getAmount());
        verify(transactionMapper).toAvro(validRequest);
        verify(kafkaProducerService).sendTransaction(mockTransaction);
        verify(metricsService).stopProcessingTimer(timerSample);
        verify(metricsService).recordTransactionProcessed();
    }

    @Test
    void processTransaction_MappingError_ThrowsException() {
        // Arrange
        when(transactionMapper.toAvro(any())).thenThrow(new RuntimeException("Mapping error"));

        // Act & Assert
        assertThrows(TransactionProcessingException.class,
                () -> transactionService.processTransaction(validRequest));

        // Verify
        verify(metricsService).recordTransactionReceived();
        verify(metricsService).recordTransactionFailed();
        verify(transactionMapper).toAvro(validRequest);
        verifyNoInteractions(kafkaProducerService);
    }

    @Test
    void processTransactions_ValidBatch_ProcessesAll() throws InterruptedException {
        // Arrange
        List<TransactionRequest> requests = Arrays.asList(
                validRequest,
                createValidTransactionRequest());

        CompletableFuture<Void> future = CompletableFuture.completedFuture(null);
        when(transactionMapper.toAvro(any())).thenReturn(mockTransaction);
        when(kafkaProducerService.sendTransaction(any())).thenReturn(future);

        // Act
        transactionService.processTransactions(requests);

        // Small delay to allow async processing to complete
        Thread.sleep(100);

        // Verify (considering async processing)
        verify(transactionMapper, times(2)).toAvro(any());
        verify(kafkaProducerService, times(2)).sendTransaction(any());
    }

    /**
     * Helper method to create a valid transaction request
     */
    private TransactionRequest createValidTransactionRequest() {
        TransactionRequest.MetadataDTO.LocationDTO location = TransactionRequest.MetadataDTO.LocationDTO.builder()
                .latitude(37.7749)
                .longitude(-122.4194)
                .build();

        TransactionRequest.MetadataDTO metadata = TransactionRequest.MetadataDTO.builder()
                .ipAddress("192.168.1.1")
                .deviceId("device-123")
                .location(location)
                .userAgent("Mozilla/5.0")
                .build();

        return TransactionRequest.builder()
                .transactionId("TX-" + System.currentTimeMillis())
                .timestamp(Instant.now())
                .amount(new BigDecimal("150.75"))
                .currency("USD")
                .customerId("CUST-123")
                .customerName("John Doe")
                .sourceId("ACCT-456")
                .sourceType("CHECKING")
                .destinationId("MERCHANT-789")
                .destinationType("MERCHANT")
                .transactionType("PURCHASE")
                .metadata(metadata)
                .build();
    }
}