package se.tink.backend.rpc;

import io.protostuff.Tag;
import se.tink.backend.core.User;

public class AnonymousUserResponse {

    @Tag(1)
    private String sessionId;
    @Tag(2)
    private User user;


    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
