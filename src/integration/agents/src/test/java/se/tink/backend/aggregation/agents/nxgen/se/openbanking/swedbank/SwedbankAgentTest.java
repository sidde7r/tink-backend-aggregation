package se.tink.backend.aggregation.agents.nxgen.se.openbanking.swedbank;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

@Ignore
public class SwedbankAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "se-Swedbank-oauth2")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
