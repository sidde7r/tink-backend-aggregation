package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.denmark.quintessentiallydk;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class QuintessentiallyAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-quintessentially-oauth2";

    private static final String MARKET = "dk";

    public QuintessentiallyAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
