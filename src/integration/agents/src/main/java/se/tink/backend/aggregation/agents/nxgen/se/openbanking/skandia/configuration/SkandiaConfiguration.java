package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

@JsonObject
public class SkandiaConfiguration implements ClientConfiguration {
    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    public String getClientId() {
        return Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));
    }

    public String getClientSecret() {
        return Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));
    }
}
