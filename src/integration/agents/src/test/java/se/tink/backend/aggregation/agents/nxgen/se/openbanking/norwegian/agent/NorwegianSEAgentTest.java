package se.tink.backend.aggregation.agents.nxgen.se.openbanking.norwegian.agent;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class NorwegianSEAgentTest {

    @Test
    public void testRefresh() throws Exception {

        new AgentIntegrationTest.Builder("se", "se-norwegian-ob")
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .setFinancialInstitutionId("norwegian")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
