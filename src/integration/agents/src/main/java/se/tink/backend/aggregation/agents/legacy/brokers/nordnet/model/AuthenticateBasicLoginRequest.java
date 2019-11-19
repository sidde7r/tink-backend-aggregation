package se.tink.backend.aggregation.agents.brokers.nordnet.model;

public class AuthenticateBasicLoginRequest {
    private final String username;
    private final String password;
    private final String session_lang;

    public AuthenticateBasicLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
        this.session_lang = "en";
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getSession_lang() {
        return session_lang;
    }
}
