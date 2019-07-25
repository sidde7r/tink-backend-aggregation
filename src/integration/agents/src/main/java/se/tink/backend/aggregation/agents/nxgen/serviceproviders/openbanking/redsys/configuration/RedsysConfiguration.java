package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.RedsysConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class RedsysConfiguration implements ClientConfiguration {

    private String baseAuthUrl;
    private String baseAPIUrl;
    private String clientId;
    private String redirectUrl;
    private String clientSigningKeyPath;
    private String clientSigningKeyPassword;
    private String clientSigningCertificate;
    private String clientSigningCertificateId;
    private String certificateId;

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

    public String getClientSigningCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificateId),
                String.format(
                        ErrorMessages.INVALID_CONFIGURATION,
                        "Client Signing Certificate (QsealC) ID"));

        return clientSigningCertificateId;
    }

    public String getCertificateId() {
        return certificateId;
    }

    public Optional<String> getClientSigningKeyPath() {
        return Optional.ofNullable(clientSigningKeyPath);
    }

    public Optional<String> getClientSigningKeyPassword() {
        return Optional.ofNullable(clientSigningKeyPassword);
    }

    public String getClientSigningCertificate() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSigningCertificate),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Signing Certificate"));
        return clientSigningCertificate;
    }
}
