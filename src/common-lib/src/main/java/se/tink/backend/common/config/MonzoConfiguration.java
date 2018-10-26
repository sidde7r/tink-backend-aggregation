package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MonzoConfiguration {

    @JsonProperty
    private String clientId;
    @JsonProperty
    private String clientSecret;
    @JsonProperty
    private String redirectUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() { return redirectUrl; }

}
