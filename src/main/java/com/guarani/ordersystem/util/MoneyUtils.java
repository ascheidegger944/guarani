package com.guarani.ordersystem.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;

public class MoneyUtils {

    private static final Locale BRAZIL_LOCALE = new Locale("pt", "BR");
    private static final NumberFormat CURRENCY_FORMAT =
            NumberFormat.getCurrencyInstance(BRAZIL_LOCALE);

    private MoneyUtils() {
        // Utility class
    }

    public static String formatToBrazilianCurrency(BigDecimal amount) {
        if (amount == null) {
            return "R$ 0,00";
        }
        return CURRENCY_FORMAT.format(amount);
    }

    public static BigDecimal calculatePercentage(BigDecimal base, BigDecimal percentage) {
        if (base == null || percentage == null) {
            return BigDecimal.ZERO;
        }
        return base.multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public static BigDecimal applyDiscount(BigDecimal amount, BigDecimal discount) {
        if (amount == null || discount == null) {
            return amount;
        }
        return amount.subtract(discount).max(BigDecimal.ZERO);
    }

    public static BigDecimal calculateTotalWithTax(BigDecimal amount, BigDecimal taxRate) {
        if (amount == null || taxRate == null) {
            return amount;
        }
        BigDecimal taxAmount = amount.multiply(taxRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return amount.add(taxAmount);
    }

    public static boolean isGreaterThanZero(BigDecimal amount) {
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public static boolean isGreaterOrEqual(BigDecimal amount1, BigDecimal amount2) {
        if (amount1 == null || amount2 == null) {
            return false;
        }
        return amount1.compareTo(amount2) >= 0;
    }
}