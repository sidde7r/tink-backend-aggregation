package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebanksor;

public final class SparebankSorConstants {

    public static final String INTEGRATION_NAME = "sparebanksor";

    private SparebankSorConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessages {
        public static final String INVALID_CONFIGURATION =
                "Invalid Configuration: %s cannot be empty or null";
        public static final String MISSING_CONFIGURATION = "Client Configuration missing.";
        public static final String MISSING_TOKEN = "Cannot find token.";
    }
}
