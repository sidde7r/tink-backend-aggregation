package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.dk.globecarddk;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class GlobecardDKAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-globecarddk-oauth2";

    private static final String MARKET = "dk";

    public GlobecardDKAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
