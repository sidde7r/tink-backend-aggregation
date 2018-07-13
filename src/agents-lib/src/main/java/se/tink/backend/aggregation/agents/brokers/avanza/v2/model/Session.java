package se.tink.backend.aggregation.agents.brokers.avanza.v2.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.common.collect.Lists;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Session {

    private List<String> authenticationSessions = Lists.newArrayList();

    public void addAuthenticationSession(String authenticationSession) {
        authenticationSessions.add(authenticationSession);
    }

    public List<String> getAuthenticationSessions() {
        return authenticationSessions;
    }

    public void setAuthenticationSessions(List<String> authenticationSessions) {
        this.authenticationSessions = authenticationSessions;
    }

    public boolean isEmpty() {
        return authenticationSessions == null || authenticationSessions.size() == 0;
    }
}
