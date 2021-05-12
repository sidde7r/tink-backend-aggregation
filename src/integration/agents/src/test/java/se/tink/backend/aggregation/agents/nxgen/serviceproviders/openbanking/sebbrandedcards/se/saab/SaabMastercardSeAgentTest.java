package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.saab;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class SaabMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-saabmastercard-ob";

    private static final String MARKET = "se";

    public SaabMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
