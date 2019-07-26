package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.norway.esso;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EssoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-esso-oauth2";

    private static final String MARKET = "no";

    public EssoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
