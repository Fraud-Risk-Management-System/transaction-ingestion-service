//// src/test/java/com/fraudrisk/controller/TransactionControllerIntegrationTest.java
//package com.fraudrisk.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fraudrisk.dto.TransactionRequest;
//import com.fraudrisk.model.Transaction;
//import com.fraudrisk.service.KafkaProducerService;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.kafka.support.SendResult;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//import java.util.Arrays;
//import java.util.concurrent.CompletableFuture;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//public class TransactionControllerIntegrationTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private KafkaProducerService kafkaProducerService;
//
//    @Test
//    public void testIngestTransaction_ValidRequest_ReturnsAccepted() throws Exception {
//        // Arrange
//        TransactionRequest request = createValidTransactionRequest();
//
//        // Mock Kafka producer to return a successful future
//        CompletableFuture<SendResult<String, Transaction>> future = new CompletableFuture<>();
//        when(kafkaProducerService.sendTransaction(any())).thenReturn(future);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/transactions")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isAccepted());
//    }
//
//    @Test
//    public void testIngestTransaction_InvalidRequest_ReturnsBadRequest() throws Exception {
//        // Arrange
//        TransactionRequest request = createValidTransactionRequest();
//        request.setTransactionId(""); // Invalid: blank transaction ID
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/transactions")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(request)))
//                .andExpect(status().isBadRequest());
//    }
//
//    @Test
//    public void testIngestBatchTransactions_ValidRequest_ReturnsAccepted() throws Exception {
//        // Arrange
//        TransactionRequest request1 = createValidTransactionRequest();
//        TransactionRequest request2 = createValidTransactionRequest();
//        request2.setTransactionId("TX-002");
//
//        // Mock Kafka producer to return a successful future
//        CompletableFuture<SendResult<String, Transaction>> future = new CompletableFuture<>();
//        when(kafkaProducerService.sendTransaction(any())).thenReturn(future);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/transactions/batch")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(Arrays.asList(request1, request2))))
//                .andExpect(status().isAccepted());
//    }
//
//    @Test
//    public void testIngestBatchTransactions_EmptyBatch_ReturnsBadRequest() throws Exception {
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/transactions/batch")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(Arrays.asList())))
//                .andExpect(status().isBadRequest());
//    }
//
//    /**
//     * Helper method to create a valid transaction request
//     */
//    private TransactionRequest createValidTransactionRequest() {
//        TransactionRequest.MetadataDTO.LocationDTO location = TransactionRequest.MetadataDTO.LocationDTO.builder()
//                .latitude(37.7749)
//                .longitude(-122.4194)
//                .build();
//
//        TransactionRequest.MetadataDTO metadata = TransactionRequest.MetadataDTO.builder()
//                .ipAddress("192.168.1.1")
//                .deviceId("device-123")
//                .location(location)
//                .userAgent("Mozilla/5.0")
//                .build();
//
//        return TransactionRequest.builder()
//                .transactionId("TX-001")
//                .timestamp(Instant.now())
//                .amount(new BigDecimal("150.75"))
//                .currency("USD")
//                .customerId("CUST-123")
//                .customerName("John Doe")
//                .sourceId("ACCT-456")
//                .sourceType("CHECKING")
//                .destinationId("MERCHANT-789")
//                .destinationType("MERCHANT")
//                .transactionType("PURCHASE")
//                .metadata(metadata)
//                .build();
//    }
//}