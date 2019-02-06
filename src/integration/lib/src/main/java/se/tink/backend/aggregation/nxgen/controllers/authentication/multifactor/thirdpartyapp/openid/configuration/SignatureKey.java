package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration;

import java.security.interfaces.RSAPrivateKey;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignatureKey {

    private String keyId;
    private String key;
    private String password;

    public String getKeyId() {
        return keyId;
    }

    public RSAPrivateKey getRSAPrivateKey() {
        return RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(key));
    }
}
