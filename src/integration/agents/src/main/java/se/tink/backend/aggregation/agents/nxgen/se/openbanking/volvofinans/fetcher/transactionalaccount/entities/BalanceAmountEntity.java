package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAmountEntity {

    private String currency;
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }
}
