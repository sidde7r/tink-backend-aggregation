package se.tink.backend.aggregation.nxgen.core.account.loan.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class InterestRateConverter {

    public static Double toDecimalValue(Double rateInPercent, int scale) {
        return BigDecimal.valueOf(rateInPercent)
                .divide(BigDecimal.valueOf(100), scale, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
