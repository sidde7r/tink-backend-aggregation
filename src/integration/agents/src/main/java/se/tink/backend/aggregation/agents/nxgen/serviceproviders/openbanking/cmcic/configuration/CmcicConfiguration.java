package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.CmcicConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class CmcicConfiguration implements ClientConfiguration {

    private String baseUrl;
    private String basePath;
    private String authBaseUrl;
    private String redirectUrl;
    private String clientId;
    private String clientSigningCertificatePath;
    private String keyId;
    private String certificateId;

    public String getKeyId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(keyId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Key ID"));

        return keyId;
    }

    public String getClientSigningCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificatePath),
                String.format(
                        ErrorMessages.INVALID_CONFIGURATION, "Client signing certificate path"));

        return clientSigningCertificatePath;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getBasePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(basePath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base Path"));

        return basePath;
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
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }

    public String getAuthBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(authBaseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Auth Base URL"));

        return authBaseUrl;
    }

    public String getCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificateId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate ID"));

        return certificateId;
    }
}
