package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbrandedcards.no.nordicchoiceclub;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.SebAgentBaseTest;

public class NordicChoiceClubNoAgentTest extends SebAgentBaseTest {

    private static final String PROVIDER_NAME = "no-nordicchoiceclub-ob";

    private static final String MARKET = "no";

    public NordicChoiceClubNoAgentTest() {
        super(PROVIDER_NAME, MARKET);
    }
}
