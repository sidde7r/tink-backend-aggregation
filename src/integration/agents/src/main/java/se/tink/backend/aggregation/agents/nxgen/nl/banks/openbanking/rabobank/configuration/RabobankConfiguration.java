package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Base64;
import org.assertj.core.util.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.utils.RabobankUtils;
import se.tink.backend.aggregation.agents.utils.crypto.Certificate;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.configuration.ClientConfiguration;
import se.tink.backend.aggregation.nxgen.http.URL;

@JsonObject
public final class RabobankConfiguration implements ClientConfiguration {

    private String baseUrl;
    private String clientId;
    private String clientSecret;
    private String clientSSLKeyPassword;
    private String clientSSLP12;
    private String redirectUrl;
    private String qsealcPem;
    private String eidasProxyBaseUrl;

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getClientSSLKeyPassword() {
        return clientSSLKeyPassword;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }

    @JsonIgnore
    public byte[] getClientSSLP12bytes() {
        final String clientSslP12String = clientSSLP12;
        return Base64.getDecoder().decode(clientSslP12String);
    }

    @JsonIgnore
    public String getClientCert() {
        return RabobankUtils.getB64EncodedX509Certificate(
                getClientSSLP12bytes(), getClientSSLKeyPassword());
    }

    @JsonIgnore
    public String getQsealcSerial() {
        Preconditions.checkNotNull(qsealcPem);
        return Certificate.getX509SerialNumber(qsealcPem);
    }

    @JsonIgnore
    public String getClientCertSerial() {
        return RabobankUtils.getCertificateSerialNumber(
                getClientSSLP12bytes(), getClientSSLKeyPassword());
    }

    @JsonIgnore
    public String getQsealCert() {
        return qsealcPem;
    }

    public RabobankUrlFactory getUrls() {
        return new RabobankUrlFactory(new URL(baseUrl));
    }

    public URL getEidasProxyBaseUrl() {
        return new URL(eidasProxyBaseUrl);
    }
}
