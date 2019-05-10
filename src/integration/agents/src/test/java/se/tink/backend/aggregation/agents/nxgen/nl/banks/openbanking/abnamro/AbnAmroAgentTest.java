package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.abnamro;

import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

// @Ignore
public class AbnAmroAgentTest {

    @Test
    public void refresh() throws Exception {
        new AgentIntegrationTest.Builder("nl", "nl-abnamro-oauth2")
                .loadCredentialsBefore(false)
                .saveCredentialsAfter(false)
                .build()
                .testRefresh();
    }
}
