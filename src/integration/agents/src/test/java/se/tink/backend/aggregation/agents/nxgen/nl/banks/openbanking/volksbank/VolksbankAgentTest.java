package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class VolksbankAgentTest {

    @Test
    public void testSandbox() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-snsbank-sandbox-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
