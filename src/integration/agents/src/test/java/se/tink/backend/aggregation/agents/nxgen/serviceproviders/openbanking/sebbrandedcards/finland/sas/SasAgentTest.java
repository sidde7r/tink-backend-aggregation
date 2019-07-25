package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.finland.sas;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SasAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "fi-sas-oauth2";

    private static final String MARKET = "fi";

    public SasAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
