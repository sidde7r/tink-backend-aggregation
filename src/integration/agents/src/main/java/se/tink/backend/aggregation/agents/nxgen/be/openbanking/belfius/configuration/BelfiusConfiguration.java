package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class BelfiusConfiguration implements ClientConfiguration {

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;
    @JsonProperty @AgentConfigParam private String redirectUrl;
    @JsonProperty @Secret private String baseUrl;
    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
