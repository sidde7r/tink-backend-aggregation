package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.dk.quintessentiallythecreditcarddk;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class QuintessentiallyTheCreditCardDKAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-quintessentiallythecreditcarddk-oauth2";

    private static final String MARKET = "dk";

    public QuintessentiallyTheCreditCardDKAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
