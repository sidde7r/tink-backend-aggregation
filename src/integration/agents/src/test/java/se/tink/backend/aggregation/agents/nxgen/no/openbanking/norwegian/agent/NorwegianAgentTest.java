package se.tink.backend.aggregation.agents.nxgen.no.openbanking.norwegian.agent;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class NorwegianAgentTest {

    @Test
    public void testRefresh() throws Exception {

        new AgentIntegrationTest.Builder("no", "no-norwegian-ob")
                .expectLoggedIn(false)
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .setFinancialInstitutionId("norwegian")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
