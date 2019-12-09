package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.fi.finnairmastercard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class FinnairMastercardFiAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "fi-finnairmastercard-oauth2";

    private static final String MARKET = "fi";

    public FinnairMastercardFiAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
