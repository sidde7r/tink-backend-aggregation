package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ExecutedEntity {
    private Double value;
    private String currency;

    public Double getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }
}
