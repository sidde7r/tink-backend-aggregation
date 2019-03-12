package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {
    private String currency;
    private Double value;

    public BalanceEntity() {

    }

    public Amount getAmount() {
        return new Amount(currency, value);
    }
}