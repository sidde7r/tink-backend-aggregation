package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.fi.finnairmastercardfi;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class FinnairMastercardFIAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "fi-finnairmastercard-oauth2";

    private static final String MARKET = "fi";

    public FinnairMastercardFIAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
