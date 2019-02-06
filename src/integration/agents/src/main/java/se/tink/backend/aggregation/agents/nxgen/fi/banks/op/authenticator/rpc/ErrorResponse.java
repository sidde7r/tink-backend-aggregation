package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.OpBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private int httpResponseCode;
    private String exception;
    private String type;
    private String errorCode;
    private String localizedMessage;

    public int getHttpResponseCode() {
        return httpResponseCode;
    }

    public String getException() {
        return exception;
    }

    public String getType() {
        return type;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getLocalizedMessage() {
        return localizedMessage;
    }

    @JsonIgnore
    public boolean isIncorrectLoginCredentials() {
        return OpBankConstants.Authentication.FALSE_CREDENTIALS.equalsIgnoreCase(errorCode) ||
                OpBankConstants.Authentication.UNAUTHENTICATED_PIN.equalsIgnoreCase(errorCode);
    }
}
