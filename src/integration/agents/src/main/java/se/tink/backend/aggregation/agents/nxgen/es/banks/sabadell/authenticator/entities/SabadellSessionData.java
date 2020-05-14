package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SabadellSessionData {

    private String username;
    private String password;
    private SessionResponse sessionResponse;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public SessionResponse getSessionResponse() {
        return sessionResponse;
    }

    public void setSessionResponse(SessionResponse sessionResponse) {
        this.sessionResponse = sessionResponse;
    }
}
