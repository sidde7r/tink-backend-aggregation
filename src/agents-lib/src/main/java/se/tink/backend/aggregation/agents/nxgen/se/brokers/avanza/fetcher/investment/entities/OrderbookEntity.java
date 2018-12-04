package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OrderbookEntity {
    private String isin;
    private String name;
    private String currency;
    private String id;
    private String type;

    public String getIsin() {
        return isin;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
