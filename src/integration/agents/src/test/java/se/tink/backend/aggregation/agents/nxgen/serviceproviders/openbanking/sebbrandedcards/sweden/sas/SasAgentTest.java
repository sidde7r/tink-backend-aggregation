package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.sweden.sas;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SasAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-sas-oauth2";

    private static final String MARKET = "se";

    public SasAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
