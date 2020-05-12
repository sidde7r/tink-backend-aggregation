package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class DemobankAgentRedirectAuthenticationTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("uk", "uk-demobank-open-banking-redirect")
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setRedirectUrl(
                                "https://127.0.0.1:7357/api/v1/credentials/third-party/callback")
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
