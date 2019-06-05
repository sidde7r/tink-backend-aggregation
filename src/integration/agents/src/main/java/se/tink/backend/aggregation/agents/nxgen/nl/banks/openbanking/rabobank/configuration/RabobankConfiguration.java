package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Base64;
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
        return "fb55c324-b87e-41bf-aa43-67b9ccc468d8"; // Tink Rabobank sandbox
    }

    public String getClientSecret() {
        return "O1uV7gH7uG7eE8iR5iN0pM2cI2iH5sH2cE0bH8xT5dB7hG7qO6"; // Tink Rabobank sandbox
    }

    public String getClientSSLKeyPassword() {
        return ""; // Tink Rabobank sandbox
    }

    public String getRedirectUrl() {
        return "https://api.staging.abnamro.tinkapp.nl/api/v1/credentials/third-party/callback";
    }

    private String getClientSSLP12() {
        // Tink Rabobank sandbox
        return "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";
    }

    @JsonIgnore
    public byte[] getClientSSLP12bytes() {
        return Base64.getDecoder().decode(getClientSSLP12());
    }

    @JsonIgnore
    public String getClientCert() {
        return RabobankUtils.getB64EncodedX509Certificate(
                getClientSSLP12bytes(), getClientSSLKeyPassword());
    }

    @JsonIgnore
    public String getClientCertSerial() {
        return RabobankUtils.getCertificateSerialNumber(
                getClientSSLP12bytes(), getClientSSLKeyPassword());
    }
}
