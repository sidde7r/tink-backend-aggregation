package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.aib;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class AIBCorporateAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-aib-business-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
