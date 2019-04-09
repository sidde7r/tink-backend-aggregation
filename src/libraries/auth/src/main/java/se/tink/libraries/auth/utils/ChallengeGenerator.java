package se.tink.libraries.auth.utils;

import java.security.SecureRandom;
import java.util.Base64;

public class ChallengeGenerator {
    private static final int BYTES_IN_CHALLENGE = 32;

    private static final SecureRandom random = new SecureRandom();

    public ChallengeGenerator() {}

    public String getRandomChallenge() {
        byte[] bytes = new byte[BYTES_IN_CHALLENGE];
        random.nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
