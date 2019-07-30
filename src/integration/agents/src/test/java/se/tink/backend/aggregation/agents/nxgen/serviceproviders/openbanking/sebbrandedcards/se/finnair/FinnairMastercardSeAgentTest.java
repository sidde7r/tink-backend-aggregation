package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.finnair;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class FinnairMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-finnair-oauth2";

    private static final String MARKET = "se";

    public FinnairMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
