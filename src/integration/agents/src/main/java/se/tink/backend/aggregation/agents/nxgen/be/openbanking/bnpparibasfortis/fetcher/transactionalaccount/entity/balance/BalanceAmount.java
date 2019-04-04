package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bnpparibasfortis.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAmount {

    private String amount;
    private String currency;

    public String getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
