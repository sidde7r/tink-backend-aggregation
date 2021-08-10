package se.tink.backend.aggregation.agents.nxgen.dk.banks.webtest;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class WebTestAgentTest {

    @Test
    public void testRefresh() throws Exception {
        AgentIntegrationTest.Builder builder =
                new AgentIntegrationTest.Builder("dk", "dk-webtest")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);

        builder.build().testRefresh();
    }
}
