package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.sweden.saab;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SaabAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-saab-oauth2";

    private static final String MARKET = "se";

    public SaabAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
