package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.openbanking;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class NordeaDKAgentTest {

    @Test
    public void testRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("dk", "dk-nordea-ob")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);

        builder.build().testRefresh();
    }
}
