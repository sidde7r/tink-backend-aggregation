package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

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

    public String getCurrency() {
        return currency;
    }
}
