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

    /**
     * This method returns a random UUIDv4 with the last four characters replaced with "feed". We
     * typically use it as "state" parameter on OAuth2-APIs. The returned value is still a valid
     * UUIDv4. The special ending of the uuids are used to reject random requests on our
     * un-authenticated third-party callback endpoint(it's not a security mitigation, just removal
     * of noise).
     *
     * @return A random, valid, UUIDv4 with tink tag attached
     */
    String randomUuidWithTinkTag();
}
