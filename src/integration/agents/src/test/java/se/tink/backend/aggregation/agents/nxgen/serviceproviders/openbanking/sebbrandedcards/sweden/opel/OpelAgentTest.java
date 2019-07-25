package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.sweden.opel;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class OpelAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-opel-oauth2";

    private static final String MARKET = "se";

    public OpelAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
