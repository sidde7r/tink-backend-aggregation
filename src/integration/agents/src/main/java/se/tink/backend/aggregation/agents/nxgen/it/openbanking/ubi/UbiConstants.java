package se.tink.backend.aggregation.agents.nxgen.it.openbanking.ubi;

public final class UbiConstants {
    public static final String INTEGRATION_NAME = "ubi";

    private UbiConstants() {
        throw new AssertionError();
    }

    public static class HeaderKeys {
        public static final String OPERATION_NAME = "operation-name";
    }

    public static class HeaderValues {
        public static final String UPDATE_PSU_DATA = "updatePsuData";
    }

    public static class FormValues {
        public static final String SCA_REDIRECT = "SCARedirect";
    }
}
