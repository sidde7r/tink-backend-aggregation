package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.utils;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class CryptoUtils {

    private CryptoUtils() {
        throw new AssertionError();
    }

    public static String getCodeVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        String codeVerifier = Base64.encodeBase64URLSafeString(code);

        return codeVerifier;
    }

    public static String getCodeChallenge(String codeVerifier) {
        byte[] bytes = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
        md.update(bytes, 0, bytes.length);
        byte[] digest = md.digest();

        return Base64.encodeBase64URLSafeString(digest);
    }
}
