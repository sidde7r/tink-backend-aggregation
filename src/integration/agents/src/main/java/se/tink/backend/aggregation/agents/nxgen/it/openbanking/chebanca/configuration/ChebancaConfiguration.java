package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class ChebancaConfiguration implements ClientConfiguration {

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;

    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    @JsonProperty @Secret private String certificateId;

    @JsonProperty @Secret private String applicationId;

    public ChebancaConfiguration() {}

    public ChebancaConfiguration(
            String clientId, String clientSecret, String certificateId, String applicationId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
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
}
