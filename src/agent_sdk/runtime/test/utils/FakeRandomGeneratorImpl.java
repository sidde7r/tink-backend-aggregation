package se.tink.agent.runtime.test.utils;

import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;
import se.tink.agent.sdk.utils.RandomGenerator;

public class FakeRandomGeneratorImpl implements RandomGenerator {

    private static final Base64.Encoder encoder = Base64.getUrlEncoder();
    private static final String VALID_V1_UUID = "e701e58a-dda4-11eb-ba80-0242ac130004";
    private static final String VALID_V4_UUID = "00000000-0000-4000-0000-000000000000";

    @Override
    public byte[] random(int size) {
        return new byte[size];
    }

    @Override
    public String randomBase64UrlEncoded(int size) {
        byte[] randomData = random(size);
        return encoder.encodeToString(randomData);
    }

    @Override
    public String randomHexEncoded(int size) {
        byte[] randomData = random(size);
        return Hex.encodeHexString(randomData);
    }

    @Override
    public String randomAlphanumeric(int size) {
        return createConstantString(size, 'A');
    }

    @Override
    public String randomAlphanumeric(int size, String alphabet) {
        return createConstantString(size, alphabet.charAt(0));
    }

    @Override
    public int randomInt(int bound) {
        return 0;
    }

    @Override
    public int randomNumberInRange(int minimum, int maximum) {
        return minimum;
    }

    @Override
    public double randomDoubleInRange(double minimum, double maximum) {
        return minimum;
    }

    @Override
    public UUID randomUUIDv4() {
        return UUID.fromString(VALID_V4_UUID);
    }

    @Override
    public UUID randomUUIDv1() {
        return UUID.fromString(VALID_V1_UUID);
    }

    @Override
    public String randomUuidWithTinkTag() {
        return randomUUIDv4().toString();
    }

    private String createConstantString(int size, char c) {
        char[] chars = new char[size];
        Arrays.fill(chars, c);
        return new String(chars);
    }
}
