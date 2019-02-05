package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankensor.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MobileEntity {
    private String number;

    public void setNumber(String number) {
        this.number = number;
    }
}
