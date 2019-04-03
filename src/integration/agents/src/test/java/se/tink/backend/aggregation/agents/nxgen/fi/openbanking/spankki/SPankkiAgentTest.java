package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.spankki;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class SPankkiAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder = new AgentIntegrationTest.Builder("fi", "fi-spankki-oauth2")
            .expectLoggedIn(false)
            .loadCredentialsBefore(false)
            .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
