package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness;

import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class RandomValueGeneratorImpl implements RandomValueGenerator {

    @Override
    public UUID getUUID() {
        return UUID.randomUUID();
    }

    @Override
    public byte[] secureRandom(int size) {
        return RandomUtils.secureRandom(size);
    }

    @Override
    public String generateRandomBase64UrlEncoded(int size) {
        return RandomUtils.generateRandomBase64UrlEncoded(size);
    }

    @Override
    public String generateRandomHexEncoded(int size) {
        return RandomUtils.generateRandomHexEncoded(size);
    }

    @Override
    public String generateRandomAlphanumeric(int size) {
        return RandomUtils.generateRandomAlphanumericString(size);
    }

    @Override
    public int randomInt(int bound) {
        return RandomUtils.randomInt(bound);
    }

    @Override
    public int generateRandomNumberInRange(int minimum, int maximum) {
        return RandomUtils.generateRandomNumberInRange(minimum, maximum);
    }

    @Override
    public String generateUuidWithTinkTag() {
        return UUIDUtils.generateUuidWithTinkTag();
    }
}
