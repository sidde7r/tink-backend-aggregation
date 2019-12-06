package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.nordicchoiceclub;

import org.junit.Ignore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

@Ignore
public class NordicChoiceClubSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-nordicchoiceclub-oauth2";

    private static final String MARKET = "se";

    public NordicChoiceClubSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
