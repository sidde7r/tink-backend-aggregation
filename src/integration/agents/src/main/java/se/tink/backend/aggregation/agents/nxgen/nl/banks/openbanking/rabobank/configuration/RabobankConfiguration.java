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

    public String getClientSSLKeyPassword() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSSLKeyPassword),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Client SSL Key Password"));
        return clientSSLKeyPassword;
    }

    public String getRedirectUrl() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(redirectUrl),
                String.format(ErrorMessages.INVALID_CONFIGURATION, "Redirect URL"));
        return redirectUrl;
    }

    public String getClientSSLP12() {
        Preconditions.checkNotNull(
                Strings.emptyToNull(clientSSLP12),
                String.format(
                        ErrorMessages.INVALID_CONFIGURATION, "PKCS12 Client SSL Certificate"));
        return clientSSLP12;
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
