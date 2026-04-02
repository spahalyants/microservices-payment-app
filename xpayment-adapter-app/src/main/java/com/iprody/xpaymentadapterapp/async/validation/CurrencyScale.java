package com.iprody.xpaymentadapterapp.async.validation;

import java.util.Map;
import java.util.Optional;

public final class CurrencyScale {

    static final int DEFAULT_SCALE = 2;

    private static final Map<String, Integer> SCALE_MAP = Map.ofEntries(
        Map.entry("JPY", 0),
        Map.entry("KRW", 0),
        Map.entry("VND", 0),
        Map.entry("IDR", 0),
        Map.entry("HUF", 0),
        Map.entry("ISK", 0),
        Map.entry("CLP", 0),
        Map.entry("PYG", 0),
        Map.entry("UGX", 0),
        Map.entry("RWF", 0),
        Map.entry("KWD", 3),
        Map.entry("BHD", 3),
        Map.entry("OMR", 3),
        Map.entry("JOD", 3),
        Map.entry("IQD", 3),
        Map.entry("LYD", 3),
        Map.entry("TND", 3)
    );

    private CurrencyScale() {
    }

    public static int scaleFor(String currencyCode) {
        return Optional.ofNullable(currencyCode)
            .map(String::toUpperCase)
            .map(SCALE_MAP::get)
            .orElse(DEFAULT_SCALE);
    }
}
