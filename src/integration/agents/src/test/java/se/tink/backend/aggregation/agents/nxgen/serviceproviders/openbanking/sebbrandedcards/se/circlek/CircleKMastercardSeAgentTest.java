package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.circlek;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class CircleKMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-circlekmastercard-oauth2";

    private static final String MARKET = "se";

    public CircleKMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
