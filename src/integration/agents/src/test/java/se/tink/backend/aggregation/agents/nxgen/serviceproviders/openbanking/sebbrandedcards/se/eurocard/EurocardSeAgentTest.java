package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.eurocard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EurocardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-eurocard-oauth2";

    private static final String MARKET = "se";

    public EurocardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
