package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.fi.saseurobonusmastercard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SasEurobonusMastercardFiAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "fi-saseurobonusmastercard-oauth2";

    private static final String MARKET = "fi";

    public SasEurobonusMastercardFiAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
