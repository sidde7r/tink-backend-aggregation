package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta;

public final class BancoPostaConstants {

    private BancoPostaConstants() {
        throw new AssertionError();
    }

    public static class ErrorValues {
        public static final String INVALID_CODE_MESSAGE = "The code you entered is not valid";
    }

    public static class UserMessages {
        public static final String SELECT_INFO =
                "Please insert authentication method index from 1 to %d \n";
        public static final String INPUT_FIELD = "index";
    }
}
