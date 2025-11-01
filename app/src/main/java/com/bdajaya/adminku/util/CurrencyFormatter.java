package com.bdajaya.adminku.util;

import java.text.NumberFormat;
import java.util.Locale;

public class CurrencyFormatter {

    // Gunakan konstanta atau builder
    private static final Locale INDONESIAN_LOCALE = new Locale.Builder()
            .setLanguage("id")
            .setRegion("ID")
            .build();

    private static final ThreadLocal<NumberFormat> CURRENCY = ThreadLocal.withInitial(() -> {
        NumberFormat nf = NumberFormat.getCurrencyInstance(INDONESIAN_LOCALE);
        nf.setMaximumFractionDigits(0);
        return nf;
    });

    private static final ThreadLocal<NumberFormat> NUMBER = ThreadLocal.withInitial(() -> {
        NumberFormat nf = NumberFormat.getNumberInstance(INDONESIAN_LOCALE);
        nf.setMaximumFractionDigits(0);
        return nf;
    });

    public static String formatCurrency(long amountInCents) {
        double rupiah = amountInCents / 100.0;
        return CURRENCY.get().format(rupiah);
    }

    public static String formatCurrencyWithoutSymbol(long amountInCents) {
        double rupiah = amountInCents / 100.0;
        return NUMBER.get().format(rupiah);
    }

    public static long parseCurrencyToLong(String currencyString) {
        try {
            String numericString = currencyString.replaceAll("[^0-9]", "");
            if (numericString.isEmpty()) {
                return 0;
            }
            return Long.parseLong(numericString) * 100;
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public static int parsePercentage(String percentageString) {
        try {
            String numericString = percentageString.replaceAll("[^0-9]", "");
            if (numericString.isEmpty()) {
                return 0;
            }
            return Integer.parseInt(numericString);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}