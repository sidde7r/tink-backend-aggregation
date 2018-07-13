package se.tink.backend.aggregation.agents.brokers.nordnet.model;

public class AuthenticateBasicLoginRequest {
    private final String username;
    private final String password;

    public AuthenticateBasicLoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
