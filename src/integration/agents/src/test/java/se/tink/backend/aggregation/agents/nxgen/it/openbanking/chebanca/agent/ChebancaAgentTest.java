package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class ChebancaAgentTest {
    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("it", "it-chebanca-oauth2")
                        .setAppId("tink")
                        .setFinancialInstitutionId("chebanca")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(true)
                        .saveCredentialsAfter(true)
                        .setFinancialInstitutionId("ddc68d1c7a184a568c3b6eefd68caa78");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
