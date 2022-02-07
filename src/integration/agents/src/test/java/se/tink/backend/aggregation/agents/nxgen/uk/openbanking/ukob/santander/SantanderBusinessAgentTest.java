package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.santander;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class SantanderBusinessAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-santander-business-ob")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("santander-uk")
                .build()
                .testRefresh();
    }
}
