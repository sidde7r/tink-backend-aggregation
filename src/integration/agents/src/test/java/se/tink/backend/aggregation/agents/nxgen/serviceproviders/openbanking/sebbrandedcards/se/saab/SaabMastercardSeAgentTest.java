package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.saab;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SaabMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-saabmastercard-oauth2";

    private static final String MARKET = "se";

    public SaabMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
