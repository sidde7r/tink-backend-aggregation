package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public abstract class MarketInfoResponse {

    private String marketPlace;
    private String name;
    private String id;
    private String isin;

    public String getMarketPlace() {
        return marketPlace;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getIsin() {
        return isin;
    }
}
