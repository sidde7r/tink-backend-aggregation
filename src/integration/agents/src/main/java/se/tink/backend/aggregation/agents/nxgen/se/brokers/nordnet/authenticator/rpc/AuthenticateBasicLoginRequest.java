package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthenticateBasicLoginRequest {
    private final String username;
    private final String password;

    @JsonProperty("session_lang")
    private final String sessionLang;

    public AuthenticateBasicLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.sessionLang = "en";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSessionLang() {
        return sessionLang;
    }
}
