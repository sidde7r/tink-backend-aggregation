package se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness;

import java.util.UUID;

public interface RandomValueGenerator {

    byte[] secureRandom(int size);

    String generateRandomBase64UrlEncoded(int size);

    String generateRandomHexEncoded(int size);

    String generateRandomAlphanumeric(int size);

    int randomInt(int bound);

    int generateRandomNumberInRange(int minimum, int maximum);

    double generateRandomDoubleInRange(double minimum, double maximum);

    UUID getUUID();

    String generateUuidWithTinkTag();
}
