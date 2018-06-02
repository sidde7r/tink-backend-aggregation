package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateUsernameRequest {
    private String username;
    private String password;
    private int maxInactiveMinutes;
    
    public AuthenticateUsernameRequest(int maxInactiveMinutes) {
        this.maxInactiveMinutes = maxInactiveMinutes;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getMaxInactiveMinutes() {
        return maxInactiveMinutes;
    }

    public void setMaxInactiveMinutes(int maxInactiveMinutes) {
        this.maxInactiveMinutes = maxInactiveMinutes;
    }

}
