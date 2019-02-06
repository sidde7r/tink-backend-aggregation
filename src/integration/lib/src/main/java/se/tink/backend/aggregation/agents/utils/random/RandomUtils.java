package se.tink.backend.aggregation.agents.utils.random;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import org.apache.commons.codec.binary.Hex;

public class RandomUtils {
    private static final Random random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();

    public static byte[] secureRandom(int size) {
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
    }

    public static String generateRandomBase64UrlEncoded(int size) {
        byte[] randomData = secureRandom(size);
        return encoder.encodeToString(randomData);
    }

    public static String generateRandomHexEncoded(int size) {
        byte[] randomData = secureRandom(size);
        return Hex.encodeHexString(randomData);
    }
}
