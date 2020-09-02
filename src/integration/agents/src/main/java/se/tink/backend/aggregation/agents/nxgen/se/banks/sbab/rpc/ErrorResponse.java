package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private String status;
    private String message;
    private String error;

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getError() {
        return error;
    }
}
