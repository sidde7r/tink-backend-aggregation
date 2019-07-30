package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.sas;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SasEurobonusMastercardNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-saseurobonusmastercard-oauth2";

    private static final String MARKET = "no";

    public SasEurobonusMastercardNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
