package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeaopenbanking.v2.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

public class NordeaConfiguration implements ClientConfiguration {
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
