package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.arbuthnotlatham;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class ArbuthnotlathamAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-arbuthnotlatham-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("bb9563b730f2493fb2f2c6779f03c860")
                .build()
                .testRefresh();
    }
}
