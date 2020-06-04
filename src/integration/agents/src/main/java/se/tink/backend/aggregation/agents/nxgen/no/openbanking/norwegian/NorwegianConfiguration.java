package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class NorwegianConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
    @Secret private String keyId;
    @Secret private String tokenUrl;

    public NorwegianConfiguration() {}

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }
}
