package se.tink.backend.aggregation.agents.banks.se.collector.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorResponse {
    private String status;

    @JsonProperty("status_message")
    private String statusMessage;

    @JsonProperty("error_code")
    private String errorCode;

    public String getStatus() {
        return status;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @JsonIgnore
    public boolean isErrorDueToRepeatedRequests() {
        return Optional.ofNullable(statusMessage)
                .orElse("")
                .toLowerCase()
                .contains("repeated calls the authentication request has been cancelled");
    }
}
