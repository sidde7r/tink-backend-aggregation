package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthenticationsPatchRequest {
    private final String cpr = null;
    private final String response;

    public AuthenticationsPatchRequest(String response) {
        this.response = response;
    }
}
