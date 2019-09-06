package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ScaSelectionRequest {

    private String authenticationMethodId;

    public ScaSelectionRequest(String authenticationMethodId) {
        this.authenticationMethodId = authenticationMethodId;
    }
}
