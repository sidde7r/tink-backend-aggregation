package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ValidateAuthRequest {
    String authMethod;
    String cardNumber;
    String response;
    boolean pinSwitch;

    public ValidateAuthRequest(String cardNumber, String response, String authMethod) {
        this.cardNumber = cardNumber;
        this.response = response;
        this.authMethod = authMethod;
        pinSwitch = false;
    }
}
