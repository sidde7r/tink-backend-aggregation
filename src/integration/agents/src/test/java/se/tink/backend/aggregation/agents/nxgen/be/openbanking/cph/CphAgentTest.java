package se.tink.backend.aggregation.agents.nxgen.be.openbanking.cph;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class CphAgentTest {
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {

        builder =
                new AgentIntegrationTest.Builder("be", "be-cph-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(false)
                        .setFinancialInstitutionId("cph")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
