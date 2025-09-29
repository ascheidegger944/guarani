package com.guarani.ordersystem.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidationUtils {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[a-zA-ZÀ-ÿ\\s]{2,255}$");

    private ValidationUtils() {
        // Utility class
    }

    public static boolean isValidEmail(String email) {
        if (email == null || email.length() > Constants.MAX_EMAIL_LENGTH) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isValidName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        return NAME_PATTERN.matcher(name.trim()).matches();
    }

    public static boolean isValidPrice(BigDecimal price) {
        if (price == null) {
            return false;
        }
        return price.compareTo(BigDecimal.valueOf(Constants.MIN_PRODUCT_PRICE)) >= 0 &&
                price.compareTo(BigDecimal.valueOf(Constants.MAX_PRODUCT_PRICE)) <= 0;
    }

    public static boolean isValidStockQuantity(Integer quantity) {
        if (quantity == null) {
            return false;
        }
        return quantity >= Constants.MIN_STOCK_QUANTITY;
    }

    public static boolean isValidOrderQuantity(Integer quantity) {
        if (quantity == null) {
            return false;
        }
        return quantity > 0 && quantity <= Constants.MAX_ORDER_QUANTITY;
    }

    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        return input.trim().replaceAll("\\s+", " ");
    }

    public static boolean isPositiveNumber(Number number) {
        if (number == null) {
            return false;
        }
        return number.doubleValue() > 0;
    }
}