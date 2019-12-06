package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private String currency;
    private Double amount;

    public AmountEntity() {}

    public AmountEntity(String currency, Double amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public Amount toAmount() {
        return new Amount(currency, amount);
    }

    public String getCurrency() {
        return currency;
    }
}
