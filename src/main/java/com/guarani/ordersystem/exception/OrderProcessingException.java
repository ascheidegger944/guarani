package com.guarani.ordersystem.exception;

public class OrderProcessingException extends BusinessException {

    private Long orderId;

    public OrderProcessingException(String message) {
        super(message);
    }

    public OrderProcessingException(String message, Long orderId) {
        super(message);
        this.orderId = orderId;
    }

    public OrderProcessingException(String message, String errorCode, Long orderId) {
        super(message, errorCode);
        this.orderId = orderId;
    }

    public OrderProcessingException(String message, Throwable cause, Long orderId) {
        super(message, cause);
        this.orderId = orderId;
    }

    public Long getOrderId() {
        return orderId;
    }
}