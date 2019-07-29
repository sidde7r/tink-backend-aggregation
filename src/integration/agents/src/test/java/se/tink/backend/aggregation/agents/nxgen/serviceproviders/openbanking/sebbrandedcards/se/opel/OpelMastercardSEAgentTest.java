package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.opel;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class OpelMastercardSEAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-opelmastercard-oauth2";

    private static final String MARKET = "se";

    public OpelMastercardSEAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
