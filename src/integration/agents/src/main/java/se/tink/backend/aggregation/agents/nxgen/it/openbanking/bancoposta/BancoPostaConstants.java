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

    public static class ErrorValues {
        public static final String INVALID_CODE = "Invalid code inserted";
        public static final String INVALID_CODE_MESSAGE = "The code you entered is not valid";
    }

    public static class UserMessages {
        public static final String SELECT_INFO = "Please select authentication method";
        public static final String SELECT_HELPER = "Select from 1 to %d";
    }
}
