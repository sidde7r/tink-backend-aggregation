package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.volvokortet;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class VolvoMastercardNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-volvomastercard-oauth2";

    private static final String MARKET = "no";

    public VolvoMastercardNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
