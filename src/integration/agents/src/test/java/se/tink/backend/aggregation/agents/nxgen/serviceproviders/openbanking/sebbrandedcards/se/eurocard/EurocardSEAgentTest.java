package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.eurocard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EurocardSEAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-eurocard-oauth2";

    private static final String MARKET = "se";

    public EurocardSEAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
