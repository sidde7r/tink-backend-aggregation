package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ServerHello {

    private String snonce;

    public String getSnonce() {
        return snonce;
    }
}
