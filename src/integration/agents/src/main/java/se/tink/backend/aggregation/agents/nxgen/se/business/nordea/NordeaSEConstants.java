package se.tink.backend.aggregation.agents.nxgen.se.business.nordea;

public class NordeaSEConstants {

    private NordeaSEConstants() {
        throw new IllegalStateException("Utility class");
    }

    public static class Headers {
        private Headers() {}

        public static final String REQUEST_ID = "x-Request-Id";
    }
}
