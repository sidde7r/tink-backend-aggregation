package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.quintessentially;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class QuintessentiallyTheCreditCardSEAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-quintessentiallythecreditcard-oauth2";

    private static final String MARKET = "se";

    public QuintessentiallyTheCreditCardSEAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
