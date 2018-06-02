package se.tink.backend.rpc;

import io.protostuff.Exclude;
import io.protostuff.Tag;
import se.tink.backend.core.UserContext;
import se.tink.backend.core.UserSession;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class UserLoginResponse {
    @Tag(1)
    protected UserContext context;
    @Exclude
    protected UserSession session;
    @JsonIgnore
    @Tag(2)
    protected String sessionId; 

    public UserContext getContext() {
        return context;
    }

    public UserSession getSession() {
        return session;
    }

    @Deprecated
    @JsonProperty
    public String getSessionId() {
        return session.getId();
    }

    public void setContext(UserContext context) {
        this.context = context;
    }

    public void setSession(UserSession session) {
        this.session = session;
        this.sessionId = session.getId();
    }
}
