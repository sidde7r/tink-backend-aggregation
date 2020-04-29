package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorMessage {
    private String errorCode;

    public String getErrorCode() {
        return errorCode;
    }
}
