package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class FidorConfiguration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    public String getClientId() {
        checkNotNull(clientId, "Client ID");
        return clientId;
    }

    public String getClientSecret() {
        checkNotNull(clientSecret, "Client Secret");
        return clientSecret;
    }

    private void checkNotNull(String secret, String secretName) {
        Preconditions.checkNotNull(
                Strings.emptyToNull(secret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, secretName));
    }
}
