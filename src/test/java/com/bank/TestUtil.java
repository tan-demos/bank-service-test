package com.bank;

import java.math.BigDecimal;
import java.util.Random;

public class TestUtil {
    public static long randomPositiveLong() {
        return new Random().nextLong(1, Long.MAX_VALUE);
    }

    public static BigDecimal randomPositiveDecimal() {
        return BigDecimal.valueOf(new Random().nextDouble(0.1, Double.MAX_VALUE));
    }
}
