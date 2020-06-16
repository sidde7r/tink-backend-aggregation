package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.configuration;

import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class BnpParibasConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
    @AgentConfigParam private String redirectUrl;
    @Secret private String keyId;
    @Secret private String authorizeUrl;
    @Secret private String tokenUrl;
    @Secret private String baseUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    public String getKeyId() {
        return keyId;
    }

    public String getTokenUrl() {
        return tokenUrl;
    }

    public String getAuthorizeUrl() {
        return authorizeUrl;
    }

    public String getBaseUrl() {
        return baseUrl;
    }
}
