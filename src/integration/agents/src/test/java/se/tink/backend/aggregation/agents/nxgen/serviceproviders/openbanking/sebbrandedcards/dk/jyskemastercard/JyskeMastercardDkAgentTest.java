package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.dk.jyskemastercard;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class JyskeMastercardDkAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "dk-jyskemastercard-oauth2";

    private static final String MARKET = "dk";

    public JyskeMastercardDkAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
