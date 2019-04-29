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
        return "jC1vD8lM0gJ0kD7pO6iQ7vQ6sM4cV6cW7oK4eR6kL8qG2sG4tB"; // Tink Rabobank sandbox
    }

    public String getClientSSLKeyPassword() {
        return "";
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
