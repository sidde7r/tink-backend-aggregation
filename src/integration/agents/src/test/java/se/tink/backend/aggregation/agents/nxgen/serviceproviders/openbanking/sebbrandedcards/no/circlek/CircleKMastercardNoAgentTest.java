package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.circlek;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class CircleKMastercardNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-circlekmastercard-ob";

    private static final String MARKET = "no";

    public CircleKMastercardNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
