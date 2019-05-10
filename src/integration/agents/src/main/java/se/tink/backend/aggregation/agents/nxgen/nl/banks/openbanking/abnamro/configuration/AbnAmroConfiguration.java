package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Base64;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro.utils.AbnAmroUtils;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;

@JsonObject
public class AbnAmroConfiguration implements ClientConfiguration {

    private String clientId;
    private String clientSecret;
    private String clientSSLKeyPassword;
    private String clientSSLP12;
    private String redirectUrl;
    private String apiKey;

    public String getApiKey() {
        return "VA6GvoqsktLZCl7G3ALbvSLxoWZ0LXHG";
    }

    public String getClientId() {
        return "TPP_test";
    }

    public String getClientSecret() {
        return "7xenkgINmhQwAe2f";
    }

    public String getClientSSLKeyPassword() {
        return "changeit";
    }

    public String getRedirectUrl() {
        return "https://localhost/auth";
    }

    public String getClientSSLP12() {
        return "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";
    }

    @JsonIgnore
    public byte[] getClientSSLP12bytes() {
        return Base64.getDecoder().decode(getClientSSLP12());
    }

    @JsonIgnore
    public String getClientCert() {
        return AbnAmroUtils.getCertificateSerialNumber(
                getClientSSLP12bytes(), getClientSSLKeyPassword());
    }

    @JsonIgnore
    public String getClientCertSerial() {
        return AbnAmroUtils.getB64EncodedX509Certificate(
                getClientSSLP12bytes(), getClientSSLKeyPassword());
    }
}
