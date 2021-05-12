package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.opel;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class OpelMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-opelmastercard-ob";

    private static final String MARKET = "se";

    public OpelMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
