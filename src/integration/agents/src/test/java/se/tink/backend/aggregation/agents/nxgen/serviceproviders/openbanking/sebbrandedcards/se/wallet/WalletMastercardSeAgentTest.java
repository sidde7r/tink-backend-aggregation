package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.wallet;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class WalletMastercardSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-walletmastercard-oauth2";

    private static final String MARKET = "se";

    public WalletMastercardSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
