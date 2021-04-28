package se.tink.backend.aggregation.agents.nxgen.se.openbanking.volvofinans;

import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.framework.AgentIntegrationTest;

public class VolvoFinansAgentTest {

    private AgentIntegrationTest.Builder builder;

    @Before
    public void setup() {
        builder =
                new AgentIntegrationTest.Builder("se", "se-volvofinans-ob")
                        .loadCredentialsBefore(false)
                        .setFinancialInstitutionId("volvofinans")
                        .setAppId("tink")
                        .saveCredentialsAfter(false)
                        .expectLoggedIn(false);
    }

    @Test
    public void testRefresh() throws Exception {
        builder.build().testRefresh();
    }
}
