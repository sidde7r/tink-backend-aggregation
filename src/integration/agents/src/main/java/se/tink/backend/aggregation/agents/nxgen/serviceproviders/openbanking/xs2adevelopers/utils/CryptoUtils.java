package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.utils;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoUtils {

    private static final Logger log = LoggerFactory.getLogger(CryptoUtils.class);

    private CryptoUtils() {}

    public static String getCodeVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        return Base64.encodeBase64URLSafeString(code);
    }

    public static String getCodeChallenge(String codeVerifier) {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest md = null;
        final String algorithm = "SHA-256";
        try {
            md = MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            log.error(
                    "Could not find MessageDigest object that implements required digest algorithm: {}",
                    algorithm,
                    e);
            throw new IllegalArgumentException(e);
        }
        md.update(bytes, 0, bytes.length);
        byte[] digest = md.digest();

        return Base64.encodeBase64URLSafeString(digest);
    }
}
