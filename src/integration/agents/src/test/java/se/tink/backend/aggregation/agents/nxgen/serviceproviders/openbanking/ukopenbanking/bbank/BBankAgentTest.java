package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.bbank;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class BBankAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-bbank-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setFinancialInstitutionId("bbank")
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
