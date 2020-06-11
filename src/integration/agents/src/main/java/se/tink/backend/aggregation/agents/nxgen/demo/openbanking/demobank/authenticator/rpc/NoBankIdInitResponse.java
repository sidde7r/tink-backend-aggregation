package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class NoBankIdInitResponse {
    private String errorCode;
    private String sessionId;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @JsonIgnore
    public boolean isAlreadyInProgress() {
        return "ALREADY_IN_PROGRESS".equals(errorCode);
    }
}
