package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost;

public final class BpostConstants {

    public static final String INTEGRATION_NAME = "bpost";
    public static final int TRANSACTION_FETCHING_RETRY_LIMIT = 15;

    private BpostConstants() {
        throw new AssertionError();
    }
}
