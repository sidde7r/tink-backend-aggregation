package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness;

import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;

public class MockRandomValueGenerator implements RandomValueGenerator {

    private static final Base64.Encoder encoder = Base64.getUrlEncoder();
    private static final String VALID_V1_UUID = "e701e58a-dda4-11eb-ba80-0242ac130004";
    private static final String VALID_V4_UUID = "00000000-0000-4000-0000-000000000000";

    @Override
    public UUID getUUID() {
        return java.util.UUID.fromString(VALID_V4_UUID);
    }

    @Override
    public UUID generateUUIDv1() {
        return java.util.UUID.fromString(VALID_V1_UUID);
    }

    @Override
    public byte[] secureRandom(int size) {
        return new byte[size];
    }

    @Override
    public String generateRandomBase64UrlEncoded(int size) {
        byte[] randomData = secureRandom(size);
        return encoder.encodeToString(randomData);
    }

    @Override
    public String generateRandomHexEncoded(int size) {
        byte[] randomData = secureRandom(size);
        return Hex.encodeHexString(randomData);
    }

    @Override
    public String generateRandomAlphanumeric(int size) {
        return createConstantString(size, 'A');
    }

    @Override
    public String generateRandomAlphanumeric(int size, String alphabet) {
        return createConstantString(size, alphabet.charAt(0));
    }

    @Override
    public int randomInt(int bound) {
        return 0;
    }

    @Override
    public int generateRandomNumberInRange(int minimum, int maximum) {
        return minimum;
    }

    @Override
    public double generateRandomDoubleInRange(double minimum, double maximum) {
        return 0;
    }

    @Override
    public String generateUuidWithTinkTag() {
        return getUUID().toString();
    }

    private String createConstantString(int size, char c) {
        char[] chars = new char[size];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}
