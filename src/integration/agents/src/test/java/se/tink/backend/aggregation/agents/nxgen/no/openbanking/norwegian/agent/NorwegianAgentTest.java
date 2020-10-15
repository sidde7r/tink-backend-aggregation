package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.agent;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class NorwegianAgentTest {

    @Test
    public void testRefresh() throws Exception {

        new AgentIntegrationTest.Builder("no", "no-norwegian-ob")
                .expectLoggedIn(false)
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .setFinancialInstitutionId("norwegian")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
