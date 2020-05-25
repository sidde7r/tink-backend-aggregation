package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.sdc.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

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
    public ExactCurrencyAmount toExactCurrencyAmount() {
        return ExactCurrencyAmount.of(BigDecimal.valueOf(value, scale), currency);
    }

    @JsonIgnore
    public ExactCurrencyAmount toExactCurrencyAmount(String defaultCurrency) {
        if (Strings.isNullOrEmpty(currency)) {
            return ExactCurrencyAmount.of(BigDecimal.valueOf(value, scale), defaultCurrency);
        } else {
            return toExactCurrencyAmount();
        }
    }
}
