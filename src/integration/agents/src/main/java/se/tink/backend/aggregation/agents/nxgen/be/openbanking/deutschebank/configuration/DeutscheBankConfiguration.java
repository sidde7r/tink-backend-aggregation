package se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.deutschebank.DeutscheBankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class DeutscheBankConfiguration implements ClientConfiguration {

    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String redirectUri;

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Base URL"));

        return baseUrl;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Client Secret"));

        return clientSecret;
    }

    public String getRedirectUri() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUri),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Redirect URI"));

        return redirectUri;
    }
}
