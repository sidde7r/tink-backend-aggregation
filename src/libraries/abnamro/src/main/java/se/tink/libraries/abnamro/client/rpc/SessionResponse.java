package se.tink.libraries.abnamro.client.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import se.tink.libraries.abnamro.client.model.SessionEntity;

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
