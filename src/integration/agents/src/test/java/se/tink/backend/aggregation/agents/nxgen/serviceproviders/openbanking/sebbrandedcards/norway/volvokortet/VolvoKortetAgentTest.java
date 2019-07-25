package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.norway.volvokortet;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class VolvoKortetAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-volvokortet-oauth2";

    private static final String MARKET = "no";

    public VolvoKortetAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
