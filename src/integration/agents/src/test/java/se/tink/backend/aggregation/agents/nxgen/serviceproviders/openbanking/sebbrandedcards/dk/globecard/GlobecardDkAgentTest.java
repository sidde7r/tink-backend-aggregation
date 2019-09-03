package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.dk.globecard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class GlobecardDkAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-globecard-ob";

    private static final String MARKET = "dk";

    public GlobecardDkAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
