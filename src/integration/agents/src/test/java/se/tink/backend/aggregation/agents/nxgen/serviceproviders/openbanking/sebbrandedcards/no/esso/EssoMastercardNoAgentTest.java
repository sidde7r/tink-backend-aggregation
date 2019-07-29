package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.esso;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class EssoMastercardNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-essomastercard-oauth2";

    private static final String MARKET = "no";

    public EssoMastercardNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
