package se.tink.backend.aggregation.agents.nxgen.be.openbanking.bpost;

public final class BpostConstants {

    public static final String INTEGRATION_NAME = "bpost";
    public static final String PRODUCTION_URL = "https://api.psd2.bpostbank.be";
    public static final int TRANSACTION_FETCHING_TIME_LIMIT = 15;

    private BpostConstants() {
        throw new AssertionError();
    }
}
