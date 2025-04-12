// src/main/java/com/fraudrisk/mapper/TransactionMapper.java
package com.fraudrisk.mapper;

import com.fraudrisk.dto.TransactionRequest;
import com.fraudrisk.model.Location;
import com.fraudrisk.model.Metadata;
import com.fraudrisk.model.Transaction;
import com.fraudrisk.util.AvroUtils;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Maps between Transaction DTOs and Avro models
 */
@Component
public class TransactionMapper {

    /**
     * Convert from REST API request to Avro Transaction model
     */
    public Transaction toAvro(TransactionRequest request) {
        // Create the metadata substructure if needed
        Metadata metadata = null;
        if (request.getMetadata() != null) {
            Metadata.Builder metadataBuilder = Metadata.newBuilder();

            // Add IP address if present
            if (request.getMetadata().getIpAddress() != null) {
                metadataBuilder.setIpAddress(request.getMetadata().getIpAddress());
            } else {
                metadataBuilder.setIpAddress(null);
            }

            // Add device ID if present
            if (request.getMetadata().getDeviceId() != null) {
                metadataBuilder.setDeviceId(request.getMetadata().getDeviceId());
            } else {
                metadataBuilder.setDeviceId(null);
            }

            // Add user agent if present
            if (request.getMetadata().getUserAgent() != null) {
                metadataBuilder.setUserAgent(request.getMetadata().getUserAgent());
            } else {
                metadataBuilder.setUserAgent(null);
            }

            // Add location if present
            if (request.getMetadata().getLocation() != null) {
                TransactionRequest.MetadataDTO.LocationDTO locDto = request.getMetadata().getLocation();

                Location.Builder locationBuilder = Location.newBuilder();
                locationBuilder.setLatitude(locDto.getLatitude());
                locationBuilder.setLongitude(locDto.getLongitude());

                metadataBuilder.setLocation(locationBuilder.build());
            } else {
                metadataBuilder.setLocation(null);
            }

            metadata = metadataBuilder.build();
        }

        // Create the main transaction object
        Transaction.Builder builder = Transaction.newBuilder()
                .setTransactionId(request.getTransactionId())
                .setTimestamp(Instant.ofEpochSecond(request.getTimestamp() != null ?
                        request.getTimestamp().toEpochMilli() :
                        Instant.now().toEpochMilli()))
                .setAmount(request.getAmount())
                .setCurrency(request.getCurrency())
                .setCustomerId(request.getCustomerId())
                .setCustomerName(request.getCustomerName() != null ?
                        request.getCustomerName() : "")
                .setSourceId(request.getSourceId())
                .setSourceType(request.getSourceType() != null ?
                        request.getSourceType() : "")
                .setDestinationId(request.getDestinationId() != null ?
                        request.getDestinationId() : "")
                .setDestinationType(request.getDestinationType() != null ?
                        request.getDestinationType() : "")
                .setTransactionType(request.getTransactionType());

        // Add metadata if it exists
        if (metadata != null) {
            builder.setMetadata(metadata);
        } else {
            // Create empty metadata to satisfy Avro schema
            builder.setMetadata(Metadata.newBuilder()
                    .setIpAddress(null)
                    .setDeviceId(null)
                    .setLocation(null)
                    .setUserAgent(null)
                    .build());
        }

        return builder.build();
    }
}