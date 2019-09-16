package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class ICSAgentTest {

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-ics-ob")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
