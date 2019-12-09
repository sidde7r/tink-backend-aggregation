package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.sj;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SjPrioMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-sjpriomastercard-oauth2";

    private static final String MARKET = "se";

    public SjPrioMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
