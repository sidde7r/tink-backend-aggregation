package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginErrorResponse {
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
}
