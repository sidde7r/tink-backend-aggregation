package se.tink.agent.sdk.models;

import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class Amount {
    private final String currency;
    private final BigDecimal value;

    Amount(String currency, BigDecimal value) {
        this.currency = currency;
        this.value = value;
    }

    public static Amount from(String currency, BigDecimal value) {
        return new Amount(currency, value);
    }

    public static Amount zero(String currency) {
        return new Amount(currency, BigDecimal.ZERO);
    }
}
