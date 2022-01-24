package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.newday.tui;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class TuiAgentTest {

    @Test
    public void test() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-newday-tui-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("bcf9c8c611374092b7b710fbd353edc4")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
