package com.fraudrisk.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain model representing a Transaction
 * This is a standard Java object used within the application, separate from
 * the Avro-generated classes used for serialization.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class TransactionModel {
    private String transactionId;
    private Instant timestamp;
    private BigDecimal amount;
    private String currency;
    private String customerId;
    private String customerName;
    private String sourceId;
    private String sourceType;
    private String destinationId;
    private String destinationType;
    private String transactionType;
    private MetadataModel metadata;

    /**
     * Convert from Avro Transaction to TransactionModel
     */
    public static TransactionModel fromAvro(Transaction avroTransaction) {
        try {
            // Extract metadata if present
            MetadataModel metadataModel = null;
            if (avroTransaction.getMetadata() != null) {
                // Extract location if present
                LocationModel locationModel = null;
                if (avroTransaction.getMetadata().getLocation() != null) {
                    locationModel = LocationModel.builder()
                            .latitude(avroTransaction.getMetadata().getLocation().getLatitude())
                            .longitude(avroTransaction.getMetadata().getLocation().getLongitude())
                            .build();
                }

                metadataModel = MetadataModel.builder()
                        .ipAddress(avroTransaction.getMetadata().getIpAddress())
                        .deviceId(avroTransaction.getMetadata().getDeviceId())
                        .userAgent(avroTransaction.getMetadata().getUserAgent())
                        .location(locationModel)
                        .build();
            }

            // Get amount directly as BigDecimal
            BigDecimal amountValue = (BigDecimal) avroTransaction.getAmount();

            // Extract timestamp directly as Instant
            Instant timestampValue = avroTransaction.getTimestamp();

            // Build and return the model
            return TransactionModel.builder()
                    .transactionId(avroTransaction.getTransactionId())
                    .timestamp(timestampValue)
                    .amount(amountValue)
                    .currency(avroTransaction.getCurrency())
                    .customerId(avroTransaction.getCustomerId())
                    .customerName(avroTransaction.getCustomerName())
                    .sourceId(avroTransaction.getSourceId())
                    .sourceType(avroTransaction.getSourceType())
                    .destinationId(avroTransaction.getDestinationId())
                    .destinationType(avroTransaction.getDestinationType())
                    .transactionType(avroTransaction.getTransactionType())
                    .metadata(metadataModel)
                    .build();
        } catch (Exception e) {
            log.error("Error converting Avro Transaction to TransactionModel: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Convert this TransactionModel to an Avro Transaction
     */
    public Transaction toAvro() {
        try {
            // Create Avro metadata
            Metadata avroMetadata = null;
            if (metadata != null) {
                // Create location if present
                Location avroLocation = null;
                if (metadata.getLocation() != null) {
                    avroLocation = Location.newBuilder()
                            .setLatitude(metadata.getLocation().getLatitude())
                            .setLongitude(metadata.getLocation().getLongitude())
                            .build();
                }

                avroMetadata = Metadata.newBuilder()
                        .setIpAddress(metadata.getIpAddress())
                        .setDeviceId(metadata.getDeviceId())
                        .setUserAgent(metadata.getUserAgent())
                        .setLocation(avroLocation)
                        .build();
            } else {
                // Create empty metadata to satisfy Avro schema
                avroMetadata = Metadata.newBuilder()
                        .setIpAddress(null)
                        .setDeviceId(null)
                        .setLocation(null)
                        .setUserAgent(null)
                        .build();
            }

            // Use Instant directly for timestamp
            Instant timestampValue = timestamp != null ? timestamp : Instant.now();

            // Build and return the Avro object with direct BigDecimal
            return Transaction.newBuilder()
                    .setTransactionId(transactionId)
                    .setTimestamp(timestampValue)
                    .setAmount(amount)  // Pass BigDecimal directly
                    .setCurrency(currency)
                    .setCustomerId(customerId)
                    .setCustomerName(customerName != null ? customerName : "")
                    .setSourceId(sourceId)
                    .setSourceType(sourceType != null ? sourceType : "")
                    .setDestinationId(destinationId != null ? destinationId : "")
                    .setDestinationType(destinationType != null ? destinationType : "")
                    .setTransactionType(transactionType)
                    .setMetadata(avroMetadata)
                    .build();
        } catch (Exception e) {
            log.error("Error converting TransactionModel to Avro Transaction: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetadataModel {
        private String ipAddress;
        private String deviceId;
        private LocationModel location;
        private String userAgent;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationModel {
        private Double latitude;
        private Double longitude;
    }
}