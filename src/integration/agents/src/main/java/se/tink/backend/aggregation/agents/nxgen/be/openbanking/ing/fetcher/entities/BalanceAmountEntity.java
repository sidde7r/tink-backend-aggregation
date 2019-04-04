package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceAmountEntity {

    private String currency;
    private Number amount;

    public String getCurrency() {
        return currency;
    }

    public Amount toAmount() {
        return new Amount(currency, amount);
    }
}
