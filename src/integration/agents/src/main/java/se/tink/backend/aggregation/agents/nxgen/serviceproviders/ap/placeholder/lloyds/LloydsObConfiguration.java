package se.tink.backend.aggregation.agents.nxgen.serviceproviders.ap.placeholder.lloyds;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.annotations.Secret;
import se.tink.backend.aggregation.annotations.SensitiveSecret;
import se.tink.backend.aggregation.configuration.agents.ClientConfiguration;
import se.tink.backend.aggregation.configuration.agents.ClientIdConfiguration;

/**
 * This is a configuration class to make other processes able to discover the secrets configuration
 * and to be compatible. E.g.: processes like upload secrets require this kind of class to work
 * properly. This Configuration class belongs to a dummy agent.
 */
@JsonObject
public class LloydsObConfiguration implements ClientConfiguration {

    private static final String INVALID_CONFIGURATION_MESSAGE =
            "Invalid Configuration: %s cannot be empty or null";

    @Secret private String softwareStatementAssertion;

    @Secret private String tokenEndpointAuthSigningAlg;

    @Secret private String tokenEndpointAuthMethod;

    @SensitiveSecret @ClientIdConfiguration private String clientId;

    @SensitiveSecret private String clientSecret;

    public String getSoftwareStatementAssertion() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(INVALID_CONFIGURATION_MESSAGE, "Software Statement Assertion"));
        return softwareStatementAssertion;
    }

    public String getTokenEndpointAuthSigningAlg() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(INVALID_CONFIGURATION_MESSAGE, "Token Endpoint Auth Signing Alg"));
        return tokenEndpointAuthSigningAlg;
    }

    public String getTokenEndpointAuthMethod() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(INVALID_CONFIGURATION_MESSAGE, "Token Endpoint Auth Method"));
        return tokenEndpointAuthMethod;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(INVALID_CONFIGURATION_MESSAGE, "Client ID"));
        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(INVALID_CONFIGURATION_MESSAGE, "Client Secret"));
        return clientSecret;
    }
}
