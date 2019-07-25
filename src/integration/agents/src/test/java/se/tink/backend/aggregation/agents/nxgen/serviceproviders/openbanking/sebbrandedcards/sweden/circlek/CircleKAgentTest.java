package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.sweden.circlek;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class CircleKAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-circlek-oauth2";

    private static final String MARKET = "se";

    public CircleKAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
