package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class SdcAmount {
    private long value;
    private int scale;
    private String localizedValue;
    private String localizedValueWithCurrency;
    private String currency;
    private String localizedValueWithCurrencyAtEnd;
    private String roundedAmountWithIsoCurrency;
    private String roundedAmountWithCurrencySymbol;
    private String localizedValueWithoutCurrency;

    public long getValue() {
        return value;
    }

    public int getScale() {
        return scale;
    }

    public String getLocalizedValue() {
        return localizedValue;
    }

    public String getLocalizedValueWithCurrency() {
        return localizedValueWithCurrency;
    }

    public String getLocalizedValueWithoutCurrency() {
        return localizedValueWithoutCurrency;
    }

    public String getCurrency() {
        return currency;
    }

    public String getLocalizedValueWithCurrencyAtEnd() {
        return localizedValueWithCurrencyAtEnd;
    }

    public String getRoundedAmountWithIsoCurrency() {
        return roundedAmountWithIsoCurrency;
    }

    public String getRoundedAmountWithCurrencySymbol() {
        return roundedAmountWithCurrencySymbol;
    }

    @JsonIgnore
    public Amount toTinkAmount() {
        return new Amount(currency, BigDecimal.valueOf(value, scale).doubleValue());
    }

    @JsonIgnore
    public Amount toTinkAmount(String currency) {
        Amount amount = toTinkAmount();

        if (amount.isEmpty()) {
            amount = new Amount(currency, BigDecimal.valueOf(value, scale).doubleValue());
        }

        return amount;
    }
}
