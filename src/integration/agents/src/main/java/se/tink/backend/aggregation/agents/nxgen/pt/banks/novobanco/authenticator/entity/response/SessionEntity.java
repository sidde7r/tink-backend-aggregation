package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.authenticator.entity.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SessionEntity {
    @JsonProperty("Application")
    private int application;

    @JsonProperty("Media")
    private int average;

    @JsonProperty("AuthCookie")
    private String authCookie;

    @JsonProperty("SessionCookie")
    private String sessionCookie;

    public String getAuthCookie() {
        return authCookie;
    }

    public String getSessionCookie() {
        return sessionCookie;
    }
}
