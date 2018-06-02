package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SbabMortgageIntegrationConfiguration {
    @JsonProperty
    private Boolean https;
    @JsonProperty
    private String password;
    @JsonProperty
    private String signBaseUrl;
    @JsonProperty
    private String targetHost;
    @JsonProperty
    private String username;

    public boolean isHttps() {
        return https;
    }
    
    public String getPassword() {
        return password;
    }
    
    public String getSignBaseUrl() {
        return signBaseUrl;
    }
    
    public String getTargetHost() {
        return targetHost;
    }
    
    public String getUsername() {
        return username;
    }
}
