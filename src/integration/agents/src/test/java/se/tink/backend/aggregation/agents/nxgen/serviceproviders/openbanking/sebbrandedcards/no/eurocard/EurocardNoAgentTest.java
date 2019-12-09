package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.eurocard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EurocardNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-eurocard-ob";

    private static final String MARKET = "no";

    public EurocardNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
