package com.guarani.ordersystem.util;

public class Constants {

    private Constants() {
        // Utility class
    }

    // Paginação
    public static final String DEFAULT_PAGE_NUMBER = "0";
    public static final String DEFAULT_PAGE_SIZE = "20";
    public static final String MAX_PAGE_SIZE = "100";

    // Cache
    public static final String CACHE_PRODUCTS = "products";
    public static final String CACHE_CATEGORIES = "categories";
    public static final String CACHE_USERS = "users";
    public static final String CACHE_ORDERS = "orders";

    // Security
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final long JWT_EXPIRATION_MS = 86400000L; // 24 horas

    // Validation
    public static final int MAX_EMAIL_LENGTH = 255;
    public static final int MAX_NAME_LENGTH = 255;
    public static final int MAX_DESCRIPTION_LENGTH = 1000;
    public static final int MAX_CATEGORY_LENGTH = 100;

    // Business Rules
    public static final int MIN_STOCK_QUANTITY = 0;
    public static final int MAX_ORDER_QUANTITY = 1000;
    public static final double MIN_PRODUCT_PRICE = 0.01;
    public static final double MAX_PRODUCT_PRICE = 999999.99;

    // Date Formats
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIME_ZONE = "America/Sao_Paulo";

    // Error Codes
    public static final String ERROR_VALIDATION = "VALIDATION_ERROR";
    public static final String ERROR_AUTHENTICATION = "AUTHENTICATION_ERROR";
    public static final String ERROR_AUTHORIZATION = "AUTHORIZATION_ERROR";
    public static final String ERROR_RESOURCE_NOT_FOUND = "RESOURCE_NOT_FOUND";
    public static final String ERROR_BUSINESS_RULE = "BUSINESS_RULE_ERROR";
    public static final String ERROR_ORDER_PROCESSING = "ORDER_PROCESSING_ERROR";
    public static final String ERROR_PAYMENT_PROCESSING = "PAYMENT_PROCESSING_ERROR";
}