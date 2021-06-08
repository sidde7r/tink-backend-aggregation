package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@EqualsAndHashCode
@JsonObject
public class AmountEntity {
    private final Double amount;
    private final String currency;

    public AmountEntity(ExactCurrencyAmount amount) {
        this.amount = amount.getDoubleValue();
        this.currency = amount.getCurrencyCode();
    }

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    @JsonProperty("amount")
    public String getAmount() {
        return String.format("%.2f", amount);
    }

    public String getCurrency() {
        return currency;
    }
}
