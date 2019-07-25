package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.norway.sebselected;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SebSelectedAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-sebselected-oauth2";

    private static final String MARKET = "no";

    public SebSelectedAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
