package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenAuthCodeRequest {
    private final String deviceIdentifier;

    public TokenAuthCodeRequest(String deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }
}
