package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration;

import com.google.api.client.repackaged.com.google.common.base.Preconditions;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.IngBaseConstants.ErrorMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class IngBaseConfiguration implements ClientConfiguration {

    private String baseUrl;
    private String clientCertificateSerial;
    private String clientCertificate;
    private String redirectUrl;
    private String certificateId;

    public String getBaseUrl() {
        return "https://api.sandbox.ing.com";
    }

    public String getClientCertificateSerial() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client certificate serial"));
        return clientCertificateSerial;
    }

    public String getClientCertificate() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client certificate"));
        return clientCertificate;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));
        return redirectUrl;
    }

    public String getCertificateId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Certificate ID"));
        return certificateId;
    }
}
