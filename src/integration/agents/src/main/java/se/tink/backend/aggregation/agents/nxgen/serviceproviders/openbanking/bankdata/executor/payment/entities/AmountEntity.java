package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankdata.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private String currency;
    private String amount;

    public AmountEntity(String currency, String amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public AmountEntity() {}

    public Amount toAmount() {
        return new Amount(currency, Double.parseDouble(amount));
    }

    public String getCurrency() {
        return currency;
    }
}
