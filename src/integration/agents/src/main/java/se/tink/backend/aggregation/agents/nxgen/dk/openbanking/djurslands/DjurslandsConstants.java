package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.djurslands;

public final class DjurslandsConstants {

    public static final String INTEGRATION_NAME = "djurslands";
    public static final String BASE_URL = "https://api.djurslandsbank.dk";
    public static final String BASE_AUTH_URL = "https://auth.djurslandsbank.dk";

    private DjurslandsConstants() {
        throw new AssertionError();
    }
}
