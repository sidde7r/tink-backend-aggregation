package se.tink.backend.aggregation.agents.nxgen.be.openbanking.axa;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class AxaAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {

        builder =
                new AgentIntegrationTest.Builder("be", "be-axa-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .setFinancialInstitutionId("0f0a1f6794ce4de7ae12972b89813fa2")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
