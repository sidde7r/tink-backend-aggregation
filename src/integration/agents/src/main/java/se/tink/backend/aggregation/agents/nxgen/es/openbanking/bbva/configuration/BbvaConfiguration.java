package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.BbvaConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BbvaConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String baseAuthUrl;
    private String baseApiUrl;

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

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Redirect URL"));

        return redirectUrl;
    }

    public String getBaseAuthUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAuthUrl),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Base Auth URL"));

        return baseAuthUrl;
    }

    public String getBaseApiUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseApiUrl),
                String.format(
                        ErrorMessages.INVALID_CONFIG_CANNOT_BE_EMPTY_OR_NULL, "Base API URL"));

        return baseApiUrl;
    }
}
