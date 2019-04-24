package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SebConfiguration implements ClientConfiguration {
    @JsonProperty private String clientId;

    @JsonProperty private String clientSecret;

    @JsonProperty private String redirectUrl;

    @JsonProperty private String baseUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
