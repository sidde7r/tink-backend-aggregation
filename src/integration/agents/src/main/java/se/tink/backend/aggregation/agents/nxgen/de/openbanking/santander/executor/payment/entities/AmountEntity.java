package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {

    private String amount;
    private String currency;

    public AmountEntity(String amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public AmountEntity() {}

    public Amount toAmount() {
        return new Amount(currency, Double.parseDouble(amount));
    }

    public String getCurrency() {
        return currency;
    }
}
