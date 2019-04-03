package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class RaiffeisenConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }
}
