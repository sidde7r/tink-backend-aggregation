package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

public class StarlingConfiguration implements ClientConfiguration {
    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String redirectUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
