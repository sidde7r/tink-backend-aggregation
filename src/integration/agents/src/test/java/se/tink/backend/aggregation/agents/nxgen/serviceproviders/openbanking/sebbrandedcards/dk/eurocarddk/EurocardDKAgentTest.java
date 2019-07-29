package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.dk.eurocarddk;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EurocardDKAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-eurocarddk-oauth2";

    private static final String MARKET = "dk";

    public EurocardDKAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
