package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.openbanking;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class NordeaDKAgentTest {

    @Test
    public void testRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("dk", "dk-nordea-openbanking")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true)
                        .expectLoggedIn(false);

        builder.build().testRefresh();
    }
}
