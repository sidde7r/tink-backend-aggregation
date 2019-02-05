package se.tink.backend.aggregation.agents.nxgen.no.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MobileEntity {
    private String number;

    public void setNumber(String number) {
        this.number = number;
    }
}
