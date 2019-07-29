package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.ingo;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class IngoMastercardSEAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-ingomastercard-oauth2";

    private static final String MARKET = "se";

    public IngoMastercardSEAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
