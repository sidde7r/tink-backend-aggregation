package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils.RabobankUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class RabobankConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String clientSSLKeyPassword;
    private String clientSSLP12;
    private String redirectUrl;

    // TODO: Remove hard-coded values once https://tinkab.atlassian.net/browse/MIYAG-350 is resolved

    public String getClientId() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientId),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client ID"));

        return "86317920-1280-442b-a311-8615c653172d";
        // return clientId;
    }

    public String getClientSecret() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSecret),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client Secret"));

        return "I8tT6vJ7hE8oO2cJ1pP4hQ7nT7dR6eK4fO7mI7nP5gW5cT3tQ7";
        // return clientSecret;
    }

    public String getClientSSLKeyPassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSSLKeyPassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client SSL Key Password"));

        return "";
        // return clientSSLKeyPassword;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));

        return "https://api.staging.abnamro.tinkapp.nl/api/v1/credentials/third-party/callback";
        // return redirectUrl;
    }

    public String getClientSSLP12() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSSLP12),
                String.format(
                        ErrorMessages.INVALID_CONFIGURATION, "PKCS12 Client SSL Certificate"));

        return "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";
        // return clientSSLP12;
    }

    @JsonIgnore
    public byte[] getClientSSLP12bytes() {
        return Base64.getDecoder().decode(getClientSSLP12());
    }

    @JsonIgnore
    public String getClientCert() {
        return RabobankUtils.getCertificateSerialNumber(
                getClientSSLP12bytes(), getClientSSLKeyPassword());
    }

    @JsonIgnore
    public String getClientCertSerial() {
        return RabobankUtils.getB64EncodedX509Certificate(
                getClientSSLP12bytes(), getClientSSLKeyPassword());
    }
}
