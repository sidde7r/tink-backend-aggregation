package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class KnabConfiguration implements ClientConfiguration {

    @JsonProperty @Secret @ClientIdConfiguration private String clientId;

    @JsonProperty @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    @JsonProperty @Secret private String psuIpAddress;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getPsuIpAddress() {
        return psuIpAddress;
    }
}
