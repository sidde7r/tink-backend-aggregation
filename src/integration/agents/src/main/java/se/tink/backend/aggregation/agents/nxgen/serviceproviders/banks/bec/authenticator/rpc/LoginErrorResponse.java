package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginErrorResponse {
    private String action;
    private String message;

    public String getAction() {
        return action;
    }

    public String getMessage() {
        return message;
    }
}
