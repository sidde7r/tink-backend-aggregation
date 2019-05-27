package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OriginalAmount {

    private Long amount;

    private String currency;

    public Long getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
