package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class HandshakeDecoded {
    private ServerHello serverHello;

    public String getSnonce() {
        return serverHello.getSnonce();
    }
}
