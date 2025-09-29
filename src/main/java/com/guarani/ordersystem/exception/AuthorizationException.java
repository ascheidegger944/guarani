package com.guarani.ordersystem.exception;

public class AuthorizationException extends BusinessException {

    public AuthorizationException(String message) {
        super(message);
    }

    public AuthorizationException(String message, String errorCode) {
        super(message, errorCode);
    }
}