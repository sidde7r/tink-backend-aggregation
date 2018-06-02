package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IntercomConfiguration {
    @JsonProperty
    private String appId;
    @JsonProperty
    private String accessToken;
    @JsonProperty
    private boolean enabled;

    public boolean isEnabled() {
        return enabled;
    }

    public String getAppId() {
        return appId;
    }

    public String getAccessToken() {
        return accessToken;
    }
}
