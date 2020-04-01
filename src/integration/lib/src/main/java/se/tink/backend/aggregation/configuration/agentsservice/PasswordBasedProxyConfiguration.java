package se.tink.backend.aggregation.configuration.agentsservice;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class PasswordBasedProxyConfiguration {
    private String host;
    private String username;
    private String password;

    public String getHost() {
        return host;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}
