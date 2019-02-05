package se.tink.backend.aggregation.nxgen.controllers.transfer.nxgen;

public final class BankTransferConstants {
    private BankTransferConstants() {
        throw new AssertionError();
    }

    public static class ErrorMessage {
        public static final String INVALID_DESTINATION =
                "Transfer failed due to invalid destination account.";
        public static final String INVALID_SOURCE =
                "Transfer failed due to invalid source account.";
    }
}
