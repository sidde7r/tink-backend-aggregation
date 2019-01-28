package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.openbanking;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class NordeaFIAgentTest {

    @Test
    public void testRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("fi", "fi-nordea-openbanking")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false);

        builder.build().testRefresh();
    }
}
