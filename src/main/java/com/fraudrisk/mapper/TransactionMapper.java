package com.fraudrisk.mapper;

import com.fraudrisk.dto.TransactionRequest;
import com.fraudrisk.model.Location;
import com.fraudrisk.model.Metadata;
import com.fraudrisk.model.Transaction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Maps between Transaction DTOs and Avro models
 */
@Component
@Slf4j
public class TransactionMapper {

    /**
     * Convert from REST API request to Avro Transaction model
     */
    public Transaction toAvro(TransactionRequest request) {
        try {
            // Create the metadata substructure if needed
            Metadata metadata = null;
            if (request.getMetadata() != null) {
                Metadata.Builder metadataBuilder = Metadata.newBuilder();

                // Add IP address if present
                metadataBuilder.setIpAddress(request.getMetadata().getIpAddress());

                // Add device ID if present
                metadataBuilder.setDeviceId(request.getMetadata().getDeviceId());

                // Add user agent if present
                metadataBuilder.setUserAgent(request.getMetadata().getUserAgent());

                // Add location if present
                if (request.getMetadata().getLocation() != null) {
                    TransactionRequest.MetadataDTO.LocationDTO locDto = request.getMetadata().getLocation();

                    Location location = Location.newBuilder()
                            .setLatitude(locDto.getLatitude())
                            .setLongitude(locDto.getLongitude())
                            .build();

                    metadataBuilder.setLocation(location);
                } else {
                    metadataBuilder.setLocation(null);
                }

                metadata = metadataBuilder.build();
            } else {
                metadata = Metadata.newBuilder()
                        .setIpAddress(null)
                        .setDeviceId(null)
                        .setLocation(null)
                        .setUserAgent(null)
                        .build();
            }

            // Get timestamp as Instant
            Instant timestamp = request.getTimestamp() != null ?
                    request.getTimestamp() :
                    Instant.now();

            // Create the transaction with all fields
            return Transaction.newBuilder()
                    .setTransactionId(request.getTransactionId())
                    .setTimestamp(timestamp)
                    .setAmount(request.getAmount())  // Pass BigDecimal directly
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
                    .setTransactionType(request.getTransactionType())
                    .setMetadata(metadata)
                    .build();
        } catch (Exception e) {
            log.error("Error converting TransactionRequest to Avro Transaction: {}", e.getMessage(), e);
            throw e;
        }
    }
}