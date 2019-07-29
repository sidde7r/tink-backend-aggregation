package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.sas;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SasEurobonusMastercardSEAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-saseurobonusmastercard-oauth2";

    private static final String MARKET = "se";

    public SasEurobonusMastercardSEAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
