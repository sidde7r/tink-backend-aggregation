package se.tink.agent.sdk.utils;

import java.util.UUID;

// TODO: this should be a library so that it can be used by other libraries that rely on
// (controllable) random data.
// TODO: e.g. the new HttpApiClient (OpenAPI)
public interface RandomGenerator {
    byte[] random(int size);

    String randomBase64UrlEncoded(int size);

    String randomHexEncoded(int size);

    String randomAlphanumeric(int size);

    String randomAlphanumeric(int size, String alphabet);

    int randomInt(int bound);

    int randomNumberInRange(int minimum, int maximum);

    double randomDoubleInRange(double minimum, double maximum);

    UUID randomUUIDv4();

    UUID randomUUIDv1();

    String randomUuidWithTinkTag();
}
