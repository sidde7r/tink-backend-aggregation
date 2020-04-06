package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.sas;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class SasEurobonusMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-saseurobonusmastercard-oauth2";

    private static final String MARKET = "se";

    public SasEurobonusMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
