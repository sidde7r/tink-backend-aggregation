package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.utils;

import com.auth0.jwt.algorithms.Algorithm;
import java.security.interfaces.RSAPrivateKey;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;

public class OpenIdSignUtils {

    private static final String RS256 = "RS256";

    public static Algorithm getSignatureAlgorithm(RSAPrivateKey privateKey) {
        return Algorithm.RSA256(null, privateKey);
    }

    public static Algorithm getSignatureAlgorithm(String privateKey64, String algorithm) {

        switch (algorithm) {
            case RS256:
                return Algorithm.RSA256(null, getPrivateKey(privateKey64, algorithm));
            default:
                throw new IllegalStateException(
                        String.format("Algorithm %s not supported", algorithm));
        }
    }

    public static RSAPrivateKey getPrivateKey(String key64, String algorithm) {

        switch (algorithm) {
            case RS256:
                return RSA.getPrivateKeyFromBytes(EncodingUtils.decodeBase64String(key64));
            default:
                throw new IllegalStateException(
                        String.format("Algorithm %s not supported", algorithm));
        }
    }
}
