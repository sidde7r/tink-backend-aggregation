package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;
import se.tink.backend.aggregation.rpc.Field;

public class ICSAgentTest {

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-ics-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();

    }
}
