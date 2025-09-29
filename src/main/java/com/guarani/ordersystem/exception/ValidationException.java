package com.guarani.ordersystem.exception;

import java.util.Set;

public class ValidationException extends BusinessException {

    private Set<String> validationErrors;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Set<String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }

    public ValidationException(String message, String errorCode, Set<String> validationErrors) {
        super(message, errorCode);
        this.validationErrors = validationErrors;
    }

    public Set<String> getValidationErrors() {
        return validationErrors;
    }
}