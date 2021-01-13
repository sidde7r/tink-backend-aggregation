package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.aib;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class AIBCorporateAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-aib-business-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("aib")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
