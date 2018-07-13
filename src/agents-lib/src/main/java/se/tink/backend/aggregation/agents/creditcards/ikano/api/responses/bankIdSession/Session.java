package se.tink.backend.aggregation.agents.creditcards.ikano.api.responses.bankIdSession;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Session {
    private String sessionId;
    private String sessionKey;

    public String getSessionId() {
        return sessionId;
    }

    public String getSessionKey() {
        return sessionKey;
    }

    @JsonProperty("_sessionId")
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    @JsonProperty("_sessionKey")
    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }
}
