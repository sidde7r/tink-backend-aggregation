package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceAmountBaseEntity {
    private String currency;
    private String amount;

    public Amount toAmount() {
        return new Amount(currency, Double.parseDouble(amount));
    }
}
