package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class KnabConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String clientId;

    @JsonProperty @AgentConfigParam private String redirectUrl;

    @JsonProperty @SensitiveSecret private String clientSecret;

    @JsonProperty @Secret private String psuIpAddress;

    public String getClientId() {
        return clientId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getPsuIpAddress() {
        return psuIpAddress;
    }
}
