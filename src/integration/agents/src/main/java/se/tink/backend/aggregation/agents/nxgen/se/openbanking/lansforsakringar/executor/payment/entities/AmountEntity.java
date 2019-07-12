package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private Double amount;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(Amount amount) {
        this.amount = amount.getValue();
        this.currency = amount.getCurrency();
    }

    public Amount toAmount() {
        return new Amount(currency, amount);
    }

    public String getCurrency() {
        return currency;
    }
}
