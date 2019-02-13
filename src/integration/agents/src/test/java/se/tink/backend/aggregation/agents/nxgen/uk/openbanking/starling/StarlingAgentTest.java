package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling;

import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class StarlingAgentTest {
    @Test
    public void testLogin() throws Exception {
        new AgentIntegrationTest.Builder("uk", "uk-starling-oauth2")
                .loadCredentialsBefore(true)
                .saveCredentialsAfter(true)
                .expectLoggedIn(false)
                .build()
                .testRefresh();
    }
}
