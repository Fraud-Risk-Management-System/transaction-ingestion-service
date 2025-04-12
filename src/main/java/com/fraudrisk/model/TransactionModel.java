// src/main/java/com/fraudrisk/model/TransactionModel.java
package com.fraudrisk.model;

import com.fraudrisk.util.AvroUtils;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Plain Java model representing a Transaction
 * This class serves as a bridge between the Avro-generated Transaction class and application code
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
        MetadataModel metadataModel = null;
        if (avroTransaction.getMetadata() != null) {
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

        return TransactionModel.builder()
                .transactionId(avroTransaction.getTransactionId())
                .timestamp(avroTransaction.getTimestamp())
                .amount(avroTransaction.getAmount())
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
    }

    /**
     * Convert to Avro Transaction
     */
    public Transaction toAvro() {
        Metadata avroMetadata = null;
        if (metadata != null) {
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

        return Transaction.newBuilder()
                .setTransactionId(transactionId)
                .setTimestamp(timestamp != null ? timestamp : Instant.now())
                .setAmount(amount)
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