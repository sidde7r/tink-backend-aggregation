package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.entities.StatusEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SpankkiResponse {
    private StatusEntity status;
    private String sessionId;
    private String requestToken;
    private boolean isLoggedIn;

    public StatusEntity getStatus() {
        return status;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getRequestToken() {
        return requestToken;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    @JsonIgnore
    public int getStatusCode() {
        if (status == null) {
            return -1;
        }

        return status.getStatusCode();
    }

    @JsonIgnore
    public boolean isOK() {
        if (status == null) {
            return false;
        }

        return status.isOK();
    }

    @JsonIgnore
    public String getErrorMessage() {
        if (status == null) {
            return "Response status is null";
        }

        return status.getErrorMessage();
    }
}
