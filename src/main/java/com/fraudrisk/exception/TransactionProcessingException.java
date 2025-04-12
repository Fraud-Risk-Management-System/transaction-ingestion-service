// src/main/java/com/fraudrisk/exception/TransactionProcessingException.java
package com.fraudrisk.exception;

public class TransactionProcessingException extends RuntimeException {

    public TransactionProcessingException(String message) {
        super(message);
    }

    public TransactionProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}