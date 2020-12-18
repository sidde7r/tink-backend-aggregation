package se.tink.backend.aggregation.agents.utils.random;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;

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

    public static String generateRandomAlphanumericString(Integer size) {
        return RandomStringUtils.random(size, 0, 0, true, true, null, random);
    }

    public static int randomInt(int bound) {
        return random.nextInt(bound);
    }

    public static int generateRandomNumberInRange(int minimum, int maximum) {
        if (minimum >= maximum) {
            throw new IllegalArgumentException("Maximum must be greater than minimum");
        }
        return random.nextInt((maximum - minimum) + 1) + minimum;
    }

    public static double generateRandomDoubleInRange(double minimum, double maximum) {
        return random.doubles(minimum, maximum).findFirst().orElse(minimum);
    }
}
