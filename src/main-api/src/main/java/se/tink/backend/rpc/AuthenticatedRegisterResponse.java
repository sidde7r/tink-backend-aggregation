package se.tink.backend.rpc;

import io.protostuff.Tag;

public class AuthenticatedRegisterResponse {
    @Tag(1)
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
