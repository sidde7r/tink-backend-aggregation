package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.volvokortet;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class VolvoMastercardNOAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-volvomastercard-oauth2";

    private static final String MARKET = "no";

    public VolvoMastercardNOAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
