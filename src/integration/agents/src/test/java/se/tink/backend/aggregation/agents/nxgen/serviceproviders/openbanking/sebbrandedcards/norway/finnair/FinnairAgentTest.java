package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.norway.finnair;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class FinnairAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-finnair-oauth2";

    private static final String MARKET = "no";

    public FinnairAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
