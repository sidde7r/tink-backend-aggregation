package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.configuration;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.SwedbankFallbackConstants.ErrorMessage;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class SwedbankPsd2Configuration implements ClientConfiguration {

    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    public String getClientId() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(clientId),
                String.format(ErrorMessage.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(clientSecret),
                String.format(ErrorMessage.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }
}
