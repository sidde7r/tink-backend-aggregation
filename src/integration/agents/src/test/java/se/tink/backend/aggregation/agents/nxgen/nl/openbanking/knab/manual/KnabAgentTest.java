package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.manual;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class KnabAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-knab-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .setFinancialInstitutionId("knab")
                .setAppId("tink")
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
