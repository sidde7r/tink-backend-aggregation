package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.esso;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class EssoMastercardNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-essomastercard-ob";

    private static final String MARKET = "no";

    public EssoMastercardNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
