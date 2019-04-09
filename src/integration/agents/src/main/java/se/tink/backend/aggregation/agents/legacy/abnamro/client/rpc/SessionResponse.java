package se.tink.backend.aggregation.agents.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.backend.aggregation.agents.abnamro.client.model.SessionEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SessionResponse {

    private SessionEntity session;

    public SessionEntity getSession() {
        return session;
    }

    public void setSession(SessionEntity session) {
        this.session = session;
    }
}
