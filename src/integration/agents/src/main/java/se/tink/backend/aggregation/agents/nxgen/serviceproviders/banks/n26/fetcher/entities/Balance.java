package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class Balance {
    private int availableBalance;
    private String currency;


    public Amount getAvailableBalance() {
        return new Amount(currency, availableBalance);
    }
}
