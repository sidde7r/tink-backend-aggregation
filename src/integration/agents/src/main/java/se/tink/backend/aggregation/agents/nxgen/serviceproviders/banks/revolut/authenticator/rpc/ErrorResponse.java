package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private String message;

    public String getMessage() {
        return message;
    }
}
