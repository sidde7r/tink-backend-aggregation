package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.natwestgroup.ulster;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class UlsterClearSpendAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-ulster-clearspend-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("0c7ba941addb428c83d6ea554ecace56")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
