package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BbvaErrorResponse {
    private String severity;

    @JsonProperty("system-error-cause")
    private String systemErrorCause;

    @JsonProperty("error-message")
    private String errorMessage;

    @JsonProperty("consumer-request-id")
    private String consumerRequestId;

    @JsonProperty("system-error-code")
    private String systemErrorCode;

    @JsonProperty("http-status")
    private int httpStatus;

    @JsonProperty("system-error-description")
    private String systemErrorDescription;

    @JsonProperty("error-code")
    private String errorCode;

    private int version;

    public String getSeverity() {
        return severity;
    }

    public String getSystemErrorCause() {
        return systemErrorCause;
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
