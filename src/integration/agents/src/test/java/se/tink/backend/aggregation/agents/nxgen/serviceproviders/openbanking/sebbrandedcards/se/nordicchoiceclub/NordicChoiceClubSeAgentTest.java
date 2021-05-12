package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.se.nordicchoiceclub;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class NordicChoiceClubSeAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "se-choicemastercard-ob";

    private static final String MARKET = "se";

    public NordicChoiceClubSeAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
