package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.norway.sas;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SasAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-sas-oauth2";

    private static final String MARKET = "no";

    public SasAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
