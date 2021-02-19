package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.cumberland;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CumberlandBusinessAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-cumberland-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .setFinancialInstitutionId("c538dfbab8584f4fb2c8a5bf3d0788f7")
                .build()
                .testRefresh();
    }
}
