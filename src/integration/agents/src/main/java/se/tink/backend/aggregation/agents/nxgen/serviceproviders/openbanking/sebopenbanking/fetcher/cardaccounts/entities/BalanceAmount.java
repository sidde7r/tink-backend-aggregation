package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAmount {

    private Double amount;

    private String currency;

    public Double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
