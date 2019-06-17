package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.BerlinGroupConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class BerlinGroupConfiguration implements ClientConfiguration {
    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String clientSigningKeyPath;
    private String clientSigningCertificatePath;
    private String clientKeyStorePath;
    private String clientKeyStorePassword;
    private String redirectUrl;

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

    public String getClientKeyStorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client KeyStore Password"));

        return clientKeyStorePassword;
    }

    public String getClientKeyStorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client KeyStore Path"));

        return clientKeyStorePath;
    }

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }

    public String getClientSigningKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningKeyPath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Signing Path"));

        return clientSigningKeyPath;
    }

    public String getClientSigningCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificatePath),
                String.format(
                        ErrorMessages.INVALID_CONFIGURATION, "Client Signing Certificate Path"));

        return clientSigningCertificatePath;
    }
}
