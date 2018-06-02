package se.tink.backend.common.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FacebookConfiguration {
    @JsonProperty
    private int appId;
    @JsonProperty
    private String appSecret;
    @JsonProperty
    private int usAppId;

    public int getAppId() {
        return appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

}
