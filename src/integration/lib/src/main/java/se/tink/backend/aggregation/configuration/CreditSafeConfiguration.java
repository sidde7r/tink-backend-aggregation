package se.tink.backend.aggregation.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CreditSafeConfiguration {
    @JsonProperty("username")
    private String username;

    @JsonProperty("password")
    private String password;

    @JsonProperty("logConsumerMonitoringTraffic")
    private boolean logConsumerMonitoringTraffic;

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

    public boolean isLogConsumerMonitoringTraffic() {
        return logConsumerMonitoringTraffic;
    }
}
