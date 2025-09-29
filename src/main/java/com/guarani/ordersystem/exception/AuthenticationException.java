package com.guarani.ordersystem.exception;

public class AuthenticationException extends BusinessException {

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, String errorCode) {
        super(message, errorCode);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}