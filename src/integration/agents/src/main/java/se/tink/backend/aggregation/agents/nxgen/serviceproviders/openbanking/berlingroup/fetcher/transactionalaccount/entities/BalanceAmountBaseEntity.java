package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceAmountBaseEntity {
    private String currency;
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public Amount toAmount() {
        return new Amount(currency, Double.parseDouble(amount));
    }
}
