package se.tink.backend.aggregation.agents.nxgen.no.openbanking.handelsbanken;

public final class HandelsbankenConstants {

    public static final String INTEGRATION_NAME = "handelsbanken-norway";

    private HandelsbankenConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }
}
