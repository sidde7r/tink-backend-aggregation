package se.tink.backend.rpc.auth.bankid;

import io.protostuff.Tag;

public class AuthenticatedLoginResponse {
    @Tag(1)
    private String sessionId;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
}
