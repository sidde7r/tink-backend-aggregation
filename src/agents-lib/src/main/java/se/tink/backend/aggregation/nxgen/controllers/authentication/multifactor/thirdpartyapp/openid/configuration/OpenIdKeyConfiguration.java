package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OpenIdKeyConfiguration {
    private String keyId;
    private String b64PrivateKey;
    private String b64PublicKey;

    public String getKeyId() {
        return keyId;
    }

    public String getB64PrivateKey() {
        return b64PrivateKey;
    }

    public String getB64PublicKey() {
        return b64PublicKey;
    }
}
