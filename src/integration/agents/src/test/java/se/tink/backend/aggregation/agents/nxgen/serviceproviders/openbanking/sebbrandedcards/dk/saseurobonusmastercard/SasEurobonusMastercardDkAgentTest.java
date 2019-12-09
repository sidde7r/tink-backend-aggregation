package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.dk.saseurobonusmastercard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SasEurobonusMastercardDkAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-saseurobonusmastercard-ob";

    private static final String MARKET = "dk";

    public SasEurobonusMastercardDkAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
