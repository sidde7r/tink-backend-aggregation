package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.AgentConfigParam;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class SantanderConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;
    @AgentConfigParam private String redirectUrl;

    public String getClientId() {
        checkNotNull(clientId, "Client ID");
        return clientId;
    }

    public String getClientSecret() {
        checkNotNull(clientSecret, "Client Secret");
        return clientSecret;
    }

    public String getRedirectUrl() {
        checkNotNull(redirectUrl, "Redirect URL");
        return redirectUrl;
    }

    private void checkNotNull(String secret, String secretName) {
        Preconditions.checkNotNull(
                Strings.emptyToNull(secret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, secretName));
    }
}
