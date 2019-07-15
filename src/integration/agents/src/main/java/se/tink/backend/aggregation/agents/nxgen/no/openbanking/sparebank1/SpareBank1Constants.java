package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1;

public final class SpareBank1Constants {

    public static final String INTEGRATION_NAME = "sparebank1";

    private SpareBank1Constants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }
}
