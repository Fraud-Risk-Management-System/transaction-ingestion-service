// src/main/java/com/fraudrisk/dto/TransactionRequest.java
package com.fraudrisk.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Data Transfer Object for incoming transaction requests
 * Includes validation rules for all fields
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionRequest {

    @NotBlank(message = "Transaction ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-_]{1,50}$", message = "Transaction ID must be alphanumeric and between 1-50 characters")
    private String transactionId;

    // @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Instant timestamp;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Digits(integer = 16, fraction = 2, message = "Amount cannot exceed 16 digits in total with 2 decimal places")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a 3-letter ISO currency code")
    private String currency;

    @NotBlank(message = "Customer ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-_]{1,50}$", message = "Customer ID must be alphanumeric and between 1-50 characters")
    private String customerId;

    @Size(max = 100, message = "Customer name cannot exceed 100 characters")
    private String customerName;

    @NotBlank(message = "Source ID is required")
    @Pattern(regexp = "^[a-zA-Z0-9-_]{1,50}$", message = "Source ID must be alphanumeric and between 1-50 characters")
    private String sourceId;

    @Size(max = 50, message = "Source type cannot exceed 50 characters")
    private String sourceType;

    @Size(max = 50, message = "Destination ID cannot exceed 50 characters")
    private String destinationId;

    @Size(max = 50, message = "Destination type cannot exceed 50 characters")
    private String destinationType;

    @NotBlank(message = "Transaction type is required")
    @Size(max = 50, message = "Transaction type cannot exceed 50 characters")
    private String transactionType;

    @Valid
    private MetadataDTO metadata;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetadataDTO {
        @Pattern(regexp = "^(?:\\d{1,3}\\.){3}\\d{1,3}$", message = "IP address must be a valid IPv4 address")
        private String ipAddress;

        @Size(max = 100, message = "Device ID cannot exceed 100 characters")
        private String deviceId;

        @Valid
        private LocationDTO location;

        @Size(max = 500, message = "User agent cannot exceed 500 characters")
        private String userAgent;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        public static class LocationDTO {
            @NotNull(message = "Latitude is required when location is provided")
            @DecimalMin(value = "-90.0", message = "Latitude must be greater than or equal to -90")
            @DecimalMax(value = "90.0", message = "Latitude must be less than or equal to 90")
            private Double latitude;

            @NotNull(message = "Longitude is required when location is provided")
            @DecimalMin(value = "-180.0", message = "Longitude must be greater than or equal to -180")
            @DecimalMax(value = "180.0", message = "Longitude must be less than or equal to 180")
            private Double longitude;
        }
    }
}