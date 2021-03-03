package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class Balance {
    private String availableBalance;
    private String currency;

    public ExactCurrencyAmount getAvailableBalance() {
        return ExactCurrencyAmount.of(availableBalance, currency);
    }
}
