package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.fi.eurocard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EurocardFiAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "fi-eurocard-oauth2";

    private static final String MARKET = "fi";

    public EurocardFiAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
