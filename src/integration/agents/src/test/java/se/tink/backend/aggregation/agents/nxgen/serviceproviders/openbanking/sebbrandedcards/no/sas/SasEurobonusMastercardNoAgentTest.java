package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.sas;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class SasEurobonusMastercardNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-saseurobonusmastercard-ob";

    private static final String MARKET = "no";

    public SasEurobonusMastercardNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
