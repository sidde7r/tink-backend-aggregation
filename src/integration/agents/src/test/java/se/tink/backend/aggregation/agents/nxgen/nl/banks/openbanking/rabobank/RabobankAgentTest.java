package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class RabobankAgentTest {

    @Test
    public void refreshSandbox() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-rabobank-sandbox-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }

    @Test
    public void refreshProduction() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-rabobank-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
