package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.SparebankConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SparebankConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String baseUrl;
    private String clientSigningKeyPath;
    private String clientSigningCertificatePath;
    private String clientKeyStorePath;
    private String clientKeyStorePassword;
    private String keyId;
    private String tppId;
    private String psuIpAdress;

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getKeyId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keyId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, keyId));

        return keyId;
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

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base url"));

        return baseUrl;
    }

    public String getClientSigningKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningKeyPath),
                String.format(
                        SparebankConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client Signing Path"));

        return clientSigningKeyPath;
    }

    public String getClientSigningCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificatePath),
                String.format(
                        SparebankConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client Signing Certificate Path"));

        return clientSigningCertificatePath;
    }

    public String getClientKeyStorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePassword),
                String.format(
                        SparebankConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client KeyStore Password"));

        return clientKeyStorePassword;
    }

    public String getTppId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(tppId),
                String.format(SparebankConstants.ErrorMessages.INVALID_CONFIGURATION, "Tpp id"));

        return tppId;
    }

    public String getClientKeyStorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePath),
                String.format(
                        SparebankConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client KeyStore Path"));

        return clientKeyStorePath;
    }

    public String getPsuIpAdress() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(psuIpAdress),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Psu ip address"));

        return psuIpAdress;
    }
}
