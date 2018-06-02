package se.tink.backend.aggregation.agents.utils.random;

import java.security.SecureRandom;

public class RandomUtils {
    public static byte[] secureRandom(int size) {
        byte[] bytes = new byte[size];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
