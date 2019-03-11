package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LoginErrorResponse {
    private String severity;
    private String errorMessage;
    private String consumerRequestId;
    private String systemErrorCode;
    private int httpStatus;
    private String systemErrorDescription;
    private String errorCode;
    private int version;

    public String getSeverity() {
        return severity;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getConsumerRequestId() {
        return consumerRequestId;
    }

    public String getSystemErrorCode() {
        return systemErrorCode;
    }

    public int getHttpStatus() {
        return httpStatus;
    }

    public String getSystemErrorDescription() {
        return systemErrorDescription;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public int getVersion() {
        return version;
    }
}
