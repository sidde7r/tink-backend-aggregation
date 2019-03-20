package se.tink.backend.aggregation.agents.nxgen.se.openbanking.resursbank;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class ResursBankAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder = new AgentIntegrationTest.Builder("se", "se-resursbank-oauth2")
            .expectLoggedIn(false)
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
