package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.globecard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class GlobecardNOAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-globecard-oauth2";

    private static final String MARKET = "no";

    public GlobecardNOAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
