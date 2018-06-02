package se.tink.backend.grpc.v1.utils;

import java.math.BigDecimal;
import java.util.Optional;
import se.tink.backend.core.Amount;
import se.tink.grpc.v1.models.CurrencyDenominatedAmount;
import se.tink.grpc.v1.models.ExactNumber;

public class NumberUtils {

    public static CurrencyDenominatedAmount toCurrencyDenominatedAmount(double value, String currencyCode) {
        return CurrencyDenominatedAmount.newBuilder().setValue(toExactNumber(value))
                .setCurrencyCode(Optional.ofNullable(currencyCode).orElse(""))
                .build();
    }

    public static ExactNumber toExactNumber(double value) {
        BigDecimal bigDecimal = BigDecimal.valueOf(value);
        return ExactNumber.newBuilder().setUnscaledValue(bigDecimal.unscaledValue().longValue())
                .setScale(bigDecimal.scale()).build();
    }

    public static double toDouble(ExactNumber exactNumber) {
        return Math.pow(10, -exactNumber.getScale()) * exactNumber.getUnscaledValue();
    }

    public static Amount toAmount(CurrencyDenominatedAmount amount) {
        return new Amount(amount.getCurrencyCode(), toDouble(amount.getValue()));
    }
}
