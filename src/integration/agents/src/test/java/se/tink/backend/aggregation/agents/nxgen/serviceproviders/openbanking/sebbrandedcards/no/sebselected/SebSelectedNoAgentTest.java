package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.sebselected;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class SebSelectedNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-sebselected-ob";

    private static final String MARKET = "no";

    public SebSelectedNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
