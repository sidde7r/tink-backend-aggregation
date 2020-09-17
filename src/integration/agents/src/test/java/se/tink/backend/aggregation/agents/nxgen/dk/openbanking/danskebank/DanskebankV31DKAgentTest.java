package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.danskebank;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class DanskebankV31DKAgentTest {

    @Test
    public void testRefresh() throws Exception {
        new AgentIntegrationTest.Builder("dk", "dk-danskebank-ob")
                .setAppId("tink")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
