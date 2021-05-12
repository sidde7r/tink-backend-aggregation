package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.sj;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class SjPrioMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-sjpriomastercard-ob";

    private static final String MARKET = "se";

    public SjPrioMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
