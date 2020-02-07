package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class KnabConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String clientId;

    @JsonProperty @SensitiveSecret private String apiKey;

    @JsonProperty @Secret private String redirectUrl;

    @JsonProperty @Secret private String clientSecret;

    private String psuIpAddress;

    private String certificateId;

    public String getClientId() {
        return clientId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Object getPsuIpAddress() {
        return psuIpAddress;
    }
}
