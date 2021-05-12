package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.nk;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class NkNyckeLnMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-nknyckelnmastercard-ob";

    private static final String MARKET = "se";

    public NkNyckeLnMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
