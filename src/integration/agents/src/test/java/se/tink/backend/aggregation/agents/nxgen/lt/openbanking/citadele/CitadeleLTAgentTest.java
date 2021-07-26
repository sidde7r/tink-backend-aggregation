package se.tink.backend.aggregation.agents.nxgen.lt.openbanking.citadele;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CitadeleLTAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("LT", "lt-citadele-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .setAppId("tink")
                .build()
                .testRefresh();
    }
}
