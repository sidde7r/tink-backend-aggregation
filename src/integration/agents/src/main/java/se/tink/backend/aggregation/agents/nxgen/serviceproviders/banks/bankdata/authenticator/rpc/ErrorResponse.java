package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {

    private int errorCode;
    private String errorMessage;
    private String debugMessage;
    private String status;

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getDebugMessage() {
        return debugMessage;
    }

    public String getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "ErrorResponse{"
                + "errorCode="
                + errorCode
                + ", errorMessage='"
                + errorMessage
                + '\''
                + ", debugMessage='"
                + debugMessage
                + '\''
                + ", status='"
                + status
                + '\''
                + '}';
    }
}
