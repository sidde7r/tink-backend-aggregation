package se.tink.backend.aggregation.agents.nxgen.it.banks.ing;

import java.security.SecureRandom;
import java.util.Base64;

public class RandomDataProvider {

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder();

    public byte[] generateRandomBytes(int numBytes) {
        byte[] bytes = new byte[numBytes];
        RANDOM.nextBytes(bytes);
        return bytes;
    }

    public String generateRandomBase64UrlEncoded(int size) {
        byte[] randomData = generateRandomBytes(size);
        return ENCODER.encodeToString(randomData);
    }
}
