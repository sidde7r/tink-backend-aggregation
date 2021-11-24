package se.tink.backend.aggregation.agents.nxgen.serviceproviders.ap.placeholder.paypal;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientSecretsConfiguration;

/**
 * This is a configuration class to make other processes able to discover the secrets configuration
 * and to be compatible. E.g.: processes like upload secrets require this kind of class to work
 * properly. This Configuration class belongs to a dummy agent.
 */
@JsonObject
public class PayPalObConfiguration implements ClientConfiguration {

    public static final String INVALID_CONFIGURATION_MESSAGE =
            "Invalid Configuration: %s cannot be empty or null";

    @Secret @ClientIdConfiguration private String clientId;
    @SensitiveSecret @ClientSecretsConfiguration private String clientSecret;

    public String getClientId() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(clientId),
                String.format(INVALID_CONFIGURATION_MESSAGE, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                com.google.common.base.Strings.emptyToNull(clientSecret),
                String.format(INVALID_CONFIGURATION_MESSAGE, "Client Secret"));

        return clientSecret;
    }
}
