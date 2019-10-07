package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class VolksbankConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String redirectUrl;
    @JsonProperty @Secret private String clientId;
    @JsonProperty @SensitiveSecret private String clientSecret;
    @JsonProperty @Secret private String certificateId;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
