package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.dk.saseurobonusmastercarddk;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SasEurobonusMastercardDKAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-saseurobonusmastercard-oauth2";

    private static final String MARKET = "dk";

    public SasEurobonusMastercardDKAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
