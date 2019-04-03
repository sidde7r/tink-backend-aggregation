package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class AktiaConfiguration implements ClientConfiguration {
    @JsonProperty private String clientId;
    @JsonProperty private String clientSecret;
    @JsonProperty private String consentId;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getConsentId() {
        return consentId;
    }
}
