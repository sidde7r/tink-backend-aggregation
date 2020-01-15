package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;

@JsonObject
public class ChebancaConfiguration implements ClientConfiguration {

    @JsonProperty @Secret private String clientId;

    @JsonProperty @SensitiveSecret private String clientSecret;

    @JsonProperty @AgentConfigParam private String redirectUrl;

    @JsonProperty @Secret private String certificateId;

    @JsonProperty @Secret private String applicationId;

    public ChebancaConfiguration() {}

    public ChebancaConfiguration(
            String clientId,
            String clientSecret,
            String redirectUrl,
            String certificateId,
            String applicationId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUrl = redirectUrl;
        this.certificateId = certificateId;
        this.applicationId = applicationId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getCertificateId() {
        return certificateId;
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
}
