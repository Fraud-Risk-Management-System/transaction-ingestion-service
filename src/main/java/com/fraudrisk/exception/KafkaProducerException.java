package com.fraudrisk.exception;

import lombok.Getter;

@Getter
public class KafkaProducerException extends RuntimeException {

    private final String transactionId;

    public KafkaProducerException(String message, String transactionId) {
        super(message);
        this.transactionId = transactionId;
    }

    public KafkaProducerException(String message, String transactionId, Throwable cause) {
        super(message, cause);
        this.transactionId = transactionId;
    }
}