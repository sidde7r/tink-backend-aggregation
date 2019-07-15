package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceAmountEntity {
    private String currency;
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public Amount toAmount() {
        return new Amount(currency, Double.parseDouble(amount));
    }
}
