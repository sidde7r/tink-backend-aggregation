package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.sweden.sj;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class SjAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-sj-oauth2";

    private static final String MARKET = "se";

    public SjAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
