package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.eurocard;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class EurocardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-eurocard-ob";

    private static final String MARKET = "se";

    public EurocardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
