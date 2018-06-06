package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AvailableBalanceEntity {
    private String type;
    private Double balance;

    public String getType() {
        return type;
    }

    public Double getBalance() {
        return balance;
    }
}
