package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.norway.eurocard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EurocardAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-eurocard-oauth2";

    private static final String MARKET = "no";

    public EurocardAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
