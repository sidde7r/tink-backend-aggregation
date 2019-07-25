package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.sweden.wallet;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class WalletAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-wallet-oauth2";

    private static final String MARKET = "se";

    public WalletAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
