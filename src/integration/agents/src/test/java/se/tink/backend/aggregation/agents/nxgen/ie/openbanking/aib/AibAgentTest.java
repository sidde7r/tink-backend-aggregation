package se.tink.backend.aggregation.agents.nxgen.ie.openbanking.aib;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class AibAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("ie", "ie-aib-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("68d3b47b0d234bb4ae3022a67ee417f9")
                .build()
                .testRefresh();
    }
}
