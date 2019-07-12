package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class SibsConfiguration implements ClientConfiguration {

    private String baseUrl;
    private String certificateId;
    private String clientId;
    private String clientSecret;
    private String redirectUrl;
    private String clientSigningCertificate;
    private String clientSigningCertificateSerialNumber;
    private String aspspCode;

    public String getBaseUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(baseUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Base URL"));

        return baseUrl;
    }

    public String getCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(certificateId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate ID"));

        return certificateId;
    }

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

    public String getClientSigningCertificate() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificate),
                String.format(
                        SibsConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client Signing Certificate"));

        return clientSigningCertificate;
    }

    public String getClientSigningCertificateSerialNumber() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificateSerialNumber),
                String.format(
                        SibsConstants.ErrorMessages.INVALID_CONFIGURATION,
                        "Client Signing Certificate Serial Number"));

        return clientSigningCertificateSerialNumber;
    }

    public String getAspspCode() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(aspspCode),
                String.format(SibsConstants.ErrorMessages.INVALID_CONFIGURATION, "Aspsp Code"));

        return aspspCode;
    }
}
