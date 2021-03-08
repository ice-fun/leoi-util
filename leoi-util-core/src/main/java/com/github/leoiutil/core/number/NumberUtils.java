package com.github.leoiutil.core.number;

import java.math.BigDecimal;

public class NumberUtils {

    public static boolean isNull(Object o) {
        return o == null;
    }

    public static int safeValue(Integer value, int defaultValue) {
        return isNull(value) ? defaultValue : value;
    }

    public static int safeValue(Integer value) {
        return safeValue(value, 0);
    }

    public static long safeValue(Long value, long defaultValue) {
        return isNull(value) ? defaultValue : value;
    }

    public static long safeValue(Long value) {
        return safeValue(value, 0L);
    }

    public static float safeValue(Float value, float defaultValue) {
        return isNull(value) ? defaultValue : value;
    }

    public static float safeValue(Float value) {
        return safeValue(value, 0.0F);
    }

    public static double safeValue(Double value) {
        return safeValue(value, 0.0D);
    }

    public static double safeValue(Double value, double defaultValue) {
        return isNull(value) ? defaultValue : value;
    }

    public static BigDecimal safeValue(BigDecimal value) {
        return safeValue(value, BigDecimal.ZERO);
    }

    public static BigDecimal safeValue(BigDecimal value, BigDecimal defaultValue) {
        return isNull(value) ? defaultValue : value;
    }
}
