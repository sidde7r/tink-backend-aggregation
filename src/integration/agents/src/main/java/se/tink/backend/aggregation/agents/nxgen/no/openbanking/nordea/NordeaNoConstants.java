package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea;

public final class NordeaNoConstants {
    private NordeaNoConstants() {
        throw new AssertionError();
    }

    public static class QueryValues {
        public static final String COUNTRY = "NO";
    }

    public static class ErrorMessages {
        public static final String PAYMENT_NOT_SUPPORTED = "Not supported type of payment.";
    }
}
