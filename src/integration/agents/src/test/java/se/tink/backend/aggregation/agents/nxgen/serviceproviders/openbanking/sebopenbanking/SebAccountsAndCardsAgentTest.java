package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SebAccountsAndCardsAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-seb-oauth2";

    public SebAccountsAndCardsAgentTest() {
        super(PROVIDER_NAME);
    }
}
