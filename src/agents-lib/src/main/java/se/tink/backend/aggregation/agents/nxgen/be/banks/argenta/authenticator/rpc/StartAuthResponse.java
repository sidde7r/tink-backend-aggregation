package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class StartAuthResponse {
    String authMethod;
    String challenge;
    String cardNumber;

    public String getAuthMethod() {
        return authMethod;
    }

    public String getChallenge() {
        return challenge;
    }
}
