package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.entities.UserMessage;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private UserMessage userMessage;
    private String mfaToken;

    @JsonProperty("error_description")
    private String errorDescription;

    private String hostUrl;
    private String type;
    private String error;
    private String title;
    private String message;
    private String usedId;
    private String status;

    public UserMessage getUserMessage() {
        return userMessage;
    }

    public String getMfaToken() {
        return mfaToken;
    }

    public String getErrorDescription() {
        return errorDescription;
    }

    public String getHostUrl() {
        return hostUrl;
    }

    public String getType() {
        return type;
    }

    public String getError() {
        return error;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getUsedId() {
        return usedId;
    }

    public String getStatus() {
        return status;
    }
}
