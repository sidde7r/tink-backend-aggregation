package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.ingo;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class IngoMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-jetmastercard-ob";

    private static final String MARKET = "se";

    public IngoMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
