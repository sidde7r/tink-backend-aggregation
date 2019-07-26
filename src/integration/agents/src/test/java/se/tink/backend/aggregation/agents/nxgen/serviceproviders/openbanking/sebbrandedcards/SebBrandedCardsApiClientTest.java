package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SebBrandedCardsApiClientTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-sebkort-oauth2";

    private static final String MARKET = "se";

    public SebBrandedCardsApiClientTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
