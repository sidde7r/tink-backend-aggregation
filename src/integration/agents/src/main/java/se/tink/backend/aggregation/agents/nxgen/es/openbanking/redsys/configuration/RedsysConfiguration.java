package se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.es.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class RedsysConfiguration implements ClientConfiguration {

    private String baseAuthUrl;
    private String baseAPIUrl;
    private String clientId;
    private String authClientId;
    private String redirectUrl;
    private String consentRedirectUrl;
    private String aspsp;
    private String clientSigningKeyPath;
    private String clientSigningCertificatePath;

    public String getBaseAuthUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAuthUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base Auth URL"));

        return baseAuthUrl;
    }

    public String getBaseAPIUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseAPIUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base API URL"));

        return baseAPIUrl;
    }

    public String getAuthClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(authClientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID for authentication"));

        return authClientId;
    }

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return clientId;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return redirectUrl;
    }

    public String getAspsp() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(aspsp),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "ASPSP"));

        return aspsp;
    }

    public String getClientSigningKeyPath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningKeyPath),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Signing Key Path"));

        return clientSigningKeyPath;
    }

    public String getClientSigningCertificatePath() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificatePath),
                String.format(
                        ErrorMessages.INVALID_CONFIGURATION, "Client Signing Certificate Path"));

        return clientSigningCertificatePath;
    }

    public String getConsentRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(consentRedirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL for consent"));

        return consentRedirectUrl;
    }
}
