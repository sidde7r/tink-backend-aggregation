package se.tink.agent.runtime.utils;

import com.fasterxml.uuid.Generators;
import com.google.common.base.Preconditions;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Random;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.RandomStringUtils;
import se.tink.agent.sdk.utils.RandomGenerator;
import se.tink.libraries.uuid.UUIDUtils;

public class RandomGeneratorImpl implements RandomGenerator {

    private static final Random random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();

    @Override
    public byte[] random(int size) {
        byte[] bytes = new byte[size];
        random.nextBytes(bytes);
        return bytes;
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
        return RandomStringUtils.random(size, 0, 0, true, true, null, random);
    }

    @Override
    public String randomAlphanumeric(int size, String alphabet) {
        return RandomStringUtils.random(size, 0, 0, true, true, alphabet.toCharArray(), random);
    }

    @Override
    public int randomInt(int bound) {
        return random.nextInt(bound);
    }

    @Override
    public int randomNumberInRange(int minimum, int maximum) {
        Preconditions.checkArgument(minimum < maximum, "Maximum must be greater than minimum");
        return random.nextInt((maximum - minimum) + 1) + minimum;
    }

    @Override
    public double randomDoubleInRange(double minimum, double maximum) {
        Preconditions.checkArgument(minimum < maximum, "Maximum must be greater than minimum");
        return random.nextDouble() * (maximum - minimum) + minimum;
    }

    @Override
    public UUID randomUUIDv4() {
        return UUID.randomUUID();
    }

    @Override
    public UUID randomUUIDv1() {
        return Generators.timeBasedGenerator().generate();
    }

    @Override
    public String randomUuidWithTinkTag() {
        return UUIDUtils.generateUuidWithTinkTag();
    }
}
