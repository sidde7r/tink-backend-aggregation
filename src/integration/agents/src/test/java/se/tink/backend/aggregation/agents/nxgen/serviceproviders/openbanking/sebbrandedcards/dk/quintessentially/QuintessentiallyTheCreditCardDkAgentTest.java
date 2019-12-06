package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.dk.quintessentially;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class QuintessentiallyTheCreditCardDkAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-quintessentially-ob";

    private static final String MARKET = "dk";

    public QuintessentiallyTheCreditCardDkAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
