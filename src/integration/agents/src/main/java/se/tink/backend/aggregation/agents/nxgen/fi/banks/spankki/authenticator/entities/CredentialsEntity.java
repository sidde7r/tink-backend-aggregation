package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CredentialsEntity {
    private String username;
    private String password;
    private String pin;
    private String token;

    public CredentialsEntity setUsername(String username) {
        this.username = username;
        return this;
    }

    public CredentialsEntity setPassword(String password) {
        this.password = password;
        return this;
    }

    public CredentialsEntity setPin(String pin) {
        this.pin = pin;
        return this;
    }

    public CredentialsEntity setToken(String token) {
        this.token = token;
        return this;
    }
}
