package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.handelsbanken;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class HandelsbankenAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder = new AgentIntegrationTest.Builder("nl", "nl-handelsbanken-oauth2")
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false)
            .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
