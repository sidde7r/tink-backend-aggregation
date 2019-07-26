package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.finland.finnair;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class FinnairAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "fi-finnair-oauth2";

    private static final String MARKET = "fi";

    public FinnairAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
