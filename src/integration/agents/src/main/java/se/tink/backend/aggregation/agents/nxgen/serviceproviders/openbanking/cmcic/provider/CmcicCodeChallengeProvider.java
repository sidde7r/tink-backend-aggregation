package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.provider;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class CmcicCodeChallengeProvider {

    public String generateCodeVerifier() {
        SecureRandom sr = new SecureRandom();
        byte[] code = new byte[32];
        sr.nextBytes(code);
        return Base64.getUrlEncoder().encodeToString(code);
    }

    public String generateCodeChallengeForCodeVerifier(String verifier) {
        try {
            byte[] bytes;
            bytes = verifier.getBytes(StandardCharsets.US_ASCII);
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(bytes, 0, bytes.length);
            byte[] digest = md.digest();
            return Base64.getUrlEncoder().encodeToString(digest);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot generate code verifier", e);
        }
    }
}
