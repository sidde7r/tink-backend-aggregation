package se.tink.backend.aggregation.agents.nxgen.at.banks.erstebank.fetcher.transactional.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    private int value;
    private int precision;
    private String currency;

    public int getValue() {
        return value;
    }

    public int getPrecision() {
        return precision;
    }

    public String getCurrency() {
        return currency;
    }

    public ExactCurrencyAmount getTinkBalance() {
        String value = Integer.toString(getValue());

        if (value.length() == 1) {
            return ExactCurrencyAmount.of(Double.parseDouble(value), getCurrency());
        } else if (value.length() == 2) {
            value = new StringBuffer(value).insert(3 - getPrecision(), ".").toString();
            double doubleValue = Double.parseDouble(value);
            return ExactCurrencyAmount.of(doubleValue, getCurrency());
        }

        value = new StringBuffer(value).insert(value.length() - getPrecision(), ".").toString();
        double doubleValue = Double.parseDouble(value);
        return ExactCurrencyAmount.of(doubleValue, getCurrency());
    }
}
