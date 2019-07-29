package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.nk;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class NkNyckelnMastercardSEAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-nknyckelnmastercard-oauth2";

    private static final String MARKET = "se";

    public NkNyckelnMastercardSEAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
