package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fetchers.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailedPortfolioResponse {
    private DetailedPortfolioEntity detailedHolding;
    private String serverTime;

    public DetailedPortfolioEntity getDetailedHolding() {
        return detailedHolding;
    }

    public String getServerTime() {
        return serverTime;
    }
}
