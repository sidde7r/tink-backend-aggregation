package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class InstrumentIdEntity {
    private String isin;
    private String market;
    private String currency;

    public String getIsin() {
        return isin;
    }

    public String getMarket() {
        return market;
    }

    public String getCurrency() {
        return currency;
    }
}

