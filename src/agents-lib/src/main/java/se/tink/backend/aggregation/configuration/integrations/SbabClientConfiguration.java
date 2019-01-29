package se.tink.backend.aggregation.configuration.integrations;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SbabClientConfiguration {
    @JsonProperty private String environment;
    @JsonProperty private String basicAuthUsername;
    @JsonProperty private String basicAuthPassword;
    @JsonProperty private String clientId;
    @JsonProperty private String accessToken;
    @JsonProperty private String redirectUri;

    public String getEnvironment() {
        return environment;
    }

    public String getBasicAuthUsername() {
        return basicAuthUsername;
    }

    public String getBasicAuthPassword() {
        return basicAuthPassword;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
