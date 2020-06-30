package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness;

import java.util.UUID;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.libraries.uuid.UUIDUtils;

public class RandomValueGeneratorImpl implements RandomValueGenerator {

    public UUID getUUID() {
        return UUID.randomUUID();
    }

    public byte[] secureRandom(int size) {
        return RandomUtils.secureRandom(size);
    }

    public String generateRandomBase64UrlEncoded(int size) {
        return RandomUtils.generateRandomBase64UrlEncoded(size);
    }

    public String generateRandomHexEncoded(int size) {
        return RandomUtils.generateRandomHexEncoded(size);
    }

    public int randomInt(int bound) {
        return RandomUtils.randomInt(bound);
    }

    public int generateRandomNumberInRange(int minimum, int maximum) {
        return RandomUtils.generateRandomNumberInRange(minimum, maximum);
    }

    public String generateUuidWithTinkTag() {
        return UUIDUtils.generateUuidWithTinkTag();
    }
}
