package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.eurocard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EurocardNOAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-eurocard-oauth2";

    private static final String MARKET = "no";

    public EurocardNOAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
