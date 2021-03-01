package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@EqualsAndHashCode
@JsonObject
public class AmountEntity {
    private Double amount;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(ExactCurrencyAmount amount) {
        this.amount = amount.getDoubleValue();
        this.currency = amount.getCurrencyCode();
    }

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    @JsonProperty
    public String getAmount() {
        return String.format("%.2f", amount);
    }

    public String getCurrency() {
        return currency;
    }
}
