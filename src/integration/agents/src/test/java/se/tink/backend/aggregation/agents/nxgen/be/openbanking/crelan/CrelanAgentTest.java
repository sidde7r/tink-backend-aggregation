package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CrelanAgentTest {
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {

        builder =
                new AgentIntegrationTest.Builder("be", "be-crelan-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("crelan")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
