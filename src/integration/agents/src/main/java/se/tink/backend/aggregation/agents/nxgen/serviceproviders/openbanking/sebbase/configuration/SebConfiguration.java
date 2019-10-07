package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SebConfiguration implements ClientConfiguration {
    @JsonProperty @Secret private String clientId;

    @JsonProperty @SensitiveSecret private String clientSecret;

    @JsonProperty @Secret private String redirectUrl;

    @JsonProperty private String baseUrl;

    @JsonProperty private String authUrl;

    @JsonProperty private String eidasQwac;

    @JsonProperty private String psuIpAddress;

    public String getAuthUrl() {
        return authUrl;
    }

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

    public String getEidasQwac() {
        return eidasQwac;
    }

    public String getPsuIpAddress() {
        return psuIpAddress;
    }
}
