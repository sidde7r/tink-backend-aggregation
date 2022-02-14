package se.tink.backend.aggregation.agents.nxgen.it.openbanking.cbi.postepay.agent;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class PostePayAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("it", "it-postepay-ob")
                        .expectLoggedIn(false)
                        .loadCredentialsBefore(false)
                        .saveCredentialsAfter(true)
                        .setFinancialInstitutionId("cbi")
                        .setAppId("tink");
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
