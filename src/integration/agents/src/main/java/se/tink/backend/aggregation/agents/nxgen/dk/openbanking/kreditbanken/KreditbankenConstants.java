package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.kreditbanken;

public final class KreditbankenConstants {

    public static final String INTEGRATION_NAME = "kreditbanken";
    public static final String BASE_URL = "https://api.kreditbanken.dk";
    public static final String BASE_AUTH_URL = "https://auth.kreditbanken.dk";

    private KreditbankenConstants() {
        throw new AssertionError();
    }
}
