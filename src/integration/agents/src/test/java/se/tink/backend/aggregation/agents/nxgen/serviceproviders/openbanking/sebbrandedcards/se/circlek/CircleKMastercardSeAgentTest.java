package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.circlek;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class CircleKMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-statoilmastercard-ob";

    private static final String MARKET = "se";

    public CircleKMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
