package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness;

import java.util.Base64;
import java.util.UUID;
import org.apache.commons.codec.binary.Hex;

public class MockRandomValueGenerator implements RandomValueGenerator {

    private static final Base64.Encoder encoder = Base64.getUrlEncoder();
    private static final String VALID_V4_UUID = "00000000-0000-4000-0000-000000000000";

    @Override
    public UUID getUUID() {
        return java.util.UUID.fromString(VALID_V4_UUID);
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
    public int randomInt(int bound) {
        return 0;
    }

    @Override
    public int generateRandomNumberInRange(int minimum, int maximum) {
        return minimum;
    }

    @Override
    public String generateUuidWithTinkTag() {
        return getUUID().toString();
    }
}
