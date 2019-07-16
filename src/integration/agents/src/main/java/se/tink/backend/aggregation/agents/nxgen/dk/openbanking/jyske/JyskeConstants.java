package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.jyske;

public final class JyskeConstants {

    public static final String BASE_URL = "https://api.jyskebank.dk";
    public static final String BASE_AUTH_URL = "https://auth.jyskebank.dk";
    public static final String INTEGRATION_NAME = "jyske";

    private JyskeConstants() {
        throw new AssertionError();
    }
}
