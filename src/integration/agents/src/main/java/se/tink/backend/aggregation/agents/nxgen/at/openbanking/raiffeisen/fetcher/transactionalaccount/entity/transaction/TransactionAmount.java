package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionAmount {

    public String currency;
    public double amount;

    public Amount toTinkAmount() {
        return new Amount(currency, amount);
    }
}
