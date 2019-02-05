package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DividendEntity {
    private String dividendDate;
    private double dividendRate;

    public String getDividendDate() {
        return dividendDate;
    }

    public double getDividendRate() {
        return dividendRate;
    }
}
