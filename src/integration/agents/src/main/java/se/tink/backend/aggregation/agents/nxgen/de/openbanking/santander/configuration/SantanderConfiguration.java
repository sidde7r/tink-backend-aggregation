package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.SantanderConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SantanderConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String iban;
    private String currency;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return clientSecret;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public String getIBAN() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(iban),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "IBAN"));

        return iban;
    }

    public String getCurrency() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(currency),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Currency"));

        return currency;
    }
}
