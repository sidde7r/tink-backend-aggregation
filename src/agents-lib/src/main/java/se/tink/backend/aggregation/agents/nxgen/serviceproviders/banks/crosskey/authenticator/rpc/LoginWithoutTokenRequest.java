package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc;

public class LoginWithoutTokenRequest {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public LoginWithoutTokenRequest setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public LoginWithoutTokenRequest setPassword(String password) {
        this.password = password;
        return this;
    }
}
