package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class RabobankAgentTest {

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-rabobank-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
