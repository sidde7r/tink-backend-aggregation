package se.tink.backend.aggregation.configuration.integrations;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabConstants.Environment;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SbabConfiguration {
    @JsonProperty private Environment environment;
    @JsonProperty private String basicAuthUsername;
    @JsonProperty private String basicAuthPassword;
    @JsonProperty private String clientId;
    @JsonProperty private String accessToken;
    @JsonProperty private String redirectUri;

    public Environment getEnvironment() {
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
