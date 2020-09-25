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
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("axa")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
