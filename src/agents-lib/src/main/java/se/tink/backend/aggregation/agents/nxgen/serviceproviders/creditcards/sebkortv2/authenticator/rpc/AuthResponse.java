package se.tink.backend.aggregation.agents.nxgen.serviceproviders.creditcards.sebkortv2.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthResponse {
    private String errorCode;
    private String message;
    private String returnCode;
    private String body;

    public boolean isSuccess() {
        return "success".equalsIgnoreCase(returnCode);
    }
}
