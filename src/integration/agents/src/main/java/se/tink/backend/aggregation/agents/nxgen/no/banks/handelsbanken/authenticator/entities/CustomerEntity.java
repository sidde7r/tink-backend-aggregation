package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CustomerEntity {
    private String profitCenter;
    private String segment;
    private String brand;

    public String getProfitCenter() {
        return profitCenter;
    }

    public String getSegment() {
        return segment;
    }

    public String getBrand() {
        return brand;
    }
}
