package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CardsEntity {
    private String maskedPan;
    private String name;

    public String getMaskedPan() {
        return maskedPan;
    }

    public String getName() {
        return name;
    }
}
