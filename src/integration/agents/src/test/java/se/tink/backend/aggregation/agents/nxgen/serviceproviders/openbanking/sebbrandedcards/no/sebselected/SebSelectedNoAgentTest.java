package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.sebselected;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SebSelectedNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-sebselected-oauth2";

    private static final String MARKET = "no";

    public SebSelectedNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
