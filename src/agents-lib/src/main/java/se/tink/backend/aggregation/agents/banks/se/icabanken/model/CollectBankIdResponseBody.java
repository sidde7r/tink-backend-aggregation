package se.tink.backend.aggregation.agents.banks.se.icabanken.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CollectBankIdResponseBody {

    private static final String STATUS_SUCCESS = "Ok";
    private static final String STATUS_FAILURE = "Failed";
    private static final String STATUS_TIMEOUT = "TimedOut";

    @JsonProperty("Status")
    private String status;
    @JsonProperty("SessionId")
    private String sessionId;
    @JsonProperty("RequestId")
    private String requestId;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isSuccess() {
        return Objects.equal(status, STATUS_SUCCESS);
    }

    public boolean isFailure() {
        return Objects.equal(status, STATUS_FAILURE);
    }

    public boolean isTimeOut() {
        return STATUS_TIMEOUT.equalsIgnoreCase(status);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
}
