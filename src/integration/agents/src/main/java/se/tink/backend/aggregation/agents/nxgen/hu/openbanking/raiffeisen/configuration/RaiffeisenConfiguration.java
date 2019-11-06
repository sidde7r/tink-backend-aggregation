package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.raiffeisen.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class RaiffeisenConfiguration implements ClientConfiguration {
    @Secret @JsonProperty private String clientId;
    @SensitiveSecret @JsonProperty private String clientSecret;
    @Secret @JsonProperty private String redirectUrl;

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
