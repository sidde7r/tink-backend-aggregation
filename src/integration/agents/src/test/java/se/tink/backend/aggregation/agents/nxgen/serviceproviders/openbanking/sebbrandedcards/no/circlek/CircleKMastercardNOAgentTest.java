package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.circlek;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class CircleKMastercardNOAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-circlekmastercard-oauth2";

    private static final String MARKET = "no";

    public CircleKMastercardNOAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
