package se.tink.libraries.math;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class MathUtils {

    public static BigDecimal ceiling(BigDecimal value, BigDecimal resolution) {
        return value.divide(resolution).setScale(0, RoundingMode.CEILING).multiply(resolution);
    }

    public static BigDecimal floor(BigDecimal value, BigDecimal resolution) {
        return value.divide(resolution).setScale(0, RoundingMode.FLOOR).multiply(resolution);
    }
}
