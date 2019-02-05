package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OrderbooksEntity {
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
