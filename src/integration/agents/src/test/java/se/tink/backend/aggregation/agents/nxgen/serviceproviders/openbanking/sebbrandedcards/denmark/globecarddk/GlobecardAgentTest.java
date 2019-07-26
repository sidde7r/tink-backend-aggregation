package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.denmark.globecarddk;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class GlobecardAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-globecard-oauth2";

    private static final String MARKET = "dk";

    public GlobecardAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
