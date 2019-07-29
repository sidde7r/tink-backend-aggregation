package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.finnair;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class FinnairMastercardNOAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-finnair-oauth2";

    private static final String MARKET = "no";

    public FinnairMastercardNOAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
