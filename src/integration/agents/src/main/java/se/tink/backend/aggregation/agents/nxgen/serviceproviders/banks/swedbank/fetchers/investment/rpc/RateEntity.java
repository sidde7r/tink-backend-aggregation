package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RateEntity {
    private String value;
    private String currency;

    public String getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }
}
