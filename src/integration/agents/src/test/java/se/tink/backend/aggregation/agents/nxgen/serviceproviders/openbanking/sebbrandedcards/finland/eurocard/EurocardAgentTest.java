package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.finland.eurocard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EurocardAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "fi-eurocard-oauth2";

    private static final String MARKET = "fi";

    public EurocardAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
