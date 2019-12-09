package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.dk.eurocard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EurocardDkAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-eurocard-ob";

    private static final String MARKET = "dk";

    public EurocardDkAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
