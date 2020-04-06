package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.volvokortet;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class VolvoMastercardNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-volvomastercard-ob";

    private static final String MARKET = "no";

    public VolvoMastercardNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
