package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta;

public final class BancoPostaConstants {
    public static final String INTEGRATION_NAME = "bancoposta";

    private BancoPostaConstants() {
        throw new AssertionError();
    }

    public static class HeaderKeys {
        public static final String OPERATION_NAME = "operation-name";
    }

    public static class HeaderValues {
        public static final String UPDATE_PSU_DATA = "updatePsuData";
    }

    public static class FormValues {
        public static final String AUTHENTICATION_METHOD_ID = "2.0";
    }
}
