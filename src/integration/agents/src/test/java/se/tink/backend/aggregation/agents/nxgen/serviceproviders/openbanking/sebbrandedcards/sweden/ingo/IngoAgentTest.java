package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.sweden.ingo;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class IngoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-ingo-oauth2";

    private static final String MARKET = "se";

    public IngoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
