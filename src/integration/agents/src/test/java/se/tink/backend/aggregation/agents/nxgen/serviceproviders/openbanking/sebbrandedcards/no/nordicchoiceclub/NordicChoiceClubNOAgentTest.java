package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.nordicchoiceclub;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class NordicChoiceClubNOAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-nordicchoiceclub-oauth2";

    private static final String MARKET = "no";

    public NordicChoiceClubNOAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
