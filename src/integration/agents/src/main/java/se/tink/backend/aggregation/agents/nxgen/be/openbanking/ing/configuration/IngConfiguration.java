package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.configuration;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.IngConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class IngConfiguration implements ClientConfiguration {

    private String baseUrl;
    private String clientId;
    private String clientCertificateId;
    private String clientSigningKeyPath;
    private String clientSigningCertificatePath;
    private String clientKeyStorePath;
    private String clientKeyStorePassword;
    private String redirectUrl;

    public String getClientCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientCertificateId),
                String.format(
                        IngConstants.ErrorMessages.INVALID_CONFIGURATION, "Client Certificate Id"));

        return clientCertificateId;
    }

    public String getClientSigningKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningKeyPath),
                String.format(
                        IngConstants.ErrorMessages.INVALID_CONFIGURATION, "Client Signing Key"));

        return clientSigningKeyPath;
    }

    public String getClientSigningCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificatePath),
                String.format(
                        IngConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client Signing Certificate"));

        return clientSigningCertificatePath;
    }

    public String getClientKeyStorePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePath),
                String.format(
                        IngConstants.ErrorMessages.INVALID_CONFIGURATION, "Client Key Store Path"));

        return clientKeyStorePath;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(IngConstants.ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public String getClientKeyStorePassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientKeyStorePassword),
                String.format(
                        IngConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client Key Store Password"));

        return clientKeyStorePassword;
    }
}
