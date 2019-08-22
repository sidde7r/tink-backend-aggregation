package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class AmountEntity {
    private String value;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(String value, String currency) {
        this.value = value;
        this.currency = currency;
    }

    public String getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public Amount parseToNegativeTinkAmount() {
        return parseToTinkAmount(-1 * getValueAsDouble());
    }

    @JsonIgnore
    public Amount parseToTinkAmount() {
        return parseToTinkAmount(getValueAsDouble());
    }

    @JsonIgnore
    private Amount parseToTinkAmount(double amount) {
        if (!Strings.isNullOrEmpty(currency)) {
            return new Amount(currency, amount);
        }

        return Amount.inEUR(amount);
    }

    @JsonIgnore
    public ExactCurrencyAmount parseToExactCurrencyAmount() {
        return new ExactCurrencyAmount(parseToTinkAmount().toBigDecimal(), currency);
    }

    @JsonIgnore
    private double getValueAsDouble() {
        return Strings.isNullOrEmpty(value) ? 0d : StringUtils.parseAmount(value);
    }
}
