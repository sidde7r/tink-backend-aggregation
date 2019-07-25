package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.norway.circlek;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class CircleKAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-circlek-oauth2";

    private static final String MARKET = "no";

    public CircleKAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
