package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.finnair;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class FinnairMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-finnairmastercard-ob";

    private static final String MARKET = "se";

    public FinnairMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
