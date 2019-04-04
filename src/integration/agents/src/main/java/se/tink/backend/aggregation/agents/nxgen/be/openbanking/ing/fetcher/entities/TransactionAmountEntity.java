package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionAmountEntity {

    private String currency;
    private Number amount;

    public Amount toAmount() {
        return new Amount(currency, amount);
    }
}
