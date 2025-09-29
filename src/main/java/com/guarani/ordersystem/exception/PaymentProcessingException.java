package com.guarani.ordersystem.exception;

public class PaymentProcessingException extends BusinessException {

    private String transactionId;

    public PaymentProcessingException(String message) {
        super(message);
    }

    public PaymentProcessingException(String message, String transactionId) {
        super(message);
        this.transactionId = transactionId;
    }

    public PaymentProcessingException(String message, String errorCode, String transactionId) {
        super(message, errorCode);
        this.transactionId = transactionId;
    }

    public String getTransactionId() {
        return transactionId;
    }
}